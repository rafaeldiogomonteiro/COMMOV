package services

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"math"
	"strings"
	"time"

	"commov/backend/internal/entity"
	"commov/backend/internal/postgres"
	"github.com/go-pdf/fpdf"
	"gorm.io/gorm"
)

type UserReportService struct {
	UserRepo          *postgres.UserRepo
	ProjectRepo       *postgres.ProjectRepo
	TaskRepo          *postgres.TaskRepo
	TaskTimeEntryRepo *postgres.TaskTimeEntryRepo
	AuthService       *AuthService
}

type UserReport struct {
	User              entity.User
	GeneratedAt       time.Time
	MemberProjects    []entity.Project
	ManagedProjects   []entity.Project
	Tasks             []entity.Task
	TimeEntries       []entity.TaskTimeEntry
	TotalTasks        int
	PendingTasks      int
	InProgressTasks   int
	CompletedTasks    int
	BlockedTasks      int
	TotalTaskTime     float64
	TotalEntryTime    float64
	TotalEstimated    float64
	AvgCompletionRate float64
}

func (s *UserReportService) BuildReport(ctx context.Context, actorUserID int, userID int) (*UserReport, error) {
	if err := s.ensureAdmin(ctx, actorUserID); err != nil {
		return nil, err
	}
	if userID <= 0 {
		return nil, validationError("userId is invalid")
	}

	user, err := s.UserRepo.GetByID(ctx, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError("user not found")
		}
		return nil, fmt.Errorf("get user: %w", err)
	}

	memberProjects, err := s.ProjectRepo.ListByUserID(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list member projects: %w", err)
	}

	managedProjects, err := s.ProjectRepo.ListManagedByUserID(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list managed projects: %w", err)
	}

	tasks, err := s.TaskRepo.ListByUserID(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list tasks: %w", err)
	}

	timeEntries, err := s.TaskTimeEntryRepo.ListByUserID(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list time entries: %w", err)
	}

	report := &UserReport{
		User:            *user,
		GeneratedAt:     time.Now().UTC(),
		MemberProjects:  memberProjects,
		ManagedProjects: managedProjects,
		Tasks:           tasks,
		TimeEntries:     timeEntries,
		TotalTasks:      len(tasks),
	}

	for _, task := range tasks {
		switch strings.ToLower(strings.TrimSpace(task.Status)) {
		case entity.TaskStatusPending:
			report.PendingTasks++
		case entity.TaskStatusInProgress:
			report.InProgressTasks++
		case entity.TaskStatusCompleted:
			report.CompletedTasks++
		case entity.TaskStatusBlocked:
			report.BlockedTasks++
		}
		report.TotalTaskTime += task.TimeSpent
		report.TotalEstimated += task.EstimatedTime
		report.AvgCompletionRate += task.CompletionRate
	}

	if report.TotalTasks > 0 {
		report.AvgCompletionRate = report.AvgCompletionRate / float64(report.TotalTasks)
	}

	for _, entry := range timeEntries {
		report.TotalEntryTime += entry.TimeSpent
	}

	return report, nil
}

func (s *UserReportService) ExportPDF(ctx context.Context, actorUserID int, userID int) ([]byte, string, error) {
	report, err := s.BuildReport(ctx, actorUserID, userID)
	if err != nil {
		return nil, "", err
	}

	pdfBytes, err := renderUserReportPDF(report)
	if err != nil {
		return nil, "", fmt.Errorf("render pdf: %w", err)
	}

	safeName := sanitizeFilename(report.User.Username)
	if safeName == "" {
		safeName = fmt.Sprintf("user-%d", report.User.UserID)
	}
	filename := fmt.Sprintf("commov-user-%s-report.pdf", safeName)

	return pdfBytes, filename, nil
}

func (s *UserReportService) ensureAdmin(ctx context.Context, actorUserID int) error {
	user, err := s.UserRepo.GetByID(ctx, actorUserID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return ErrUnauthorized
		}
		return fmt.Errorf("get actor: %w", err)
	}
	if user.Role != entity.UserRoleAdmin {
		return ErrForbidden
	}
	return nil
}

func renderUserReportPDF(report *UserReport) ([]byte, error) {
	pdf := fpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(15, 15, 15)
	pdf.SetAutoPageBreak(true, 15)
	pdf.AddPage()

	primary := []int{37, 99, 235}
	muted := []int{100, 116, 139}

	pdf.SetFont("Helvetica", "B", 20)
	pdf.SetTextColor(primary[0], primary[1], primary[2])
	pdf.CellFormat(0, 12, "ComMov User Statistics Report", "", 1, "L", false, 0, "")

	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(muted[0], muted[1], muted[2])
	pdf.CellFormat(0, 6, fmt.Sprintf("Generated: %s UTC", report.GeneratedAt.Format("2006-01-02 15:04")), "", 1, "L", false, 0, "")
	pdf.Ln(4)

	sectionTitle(pdf, "Profile", primary)
	profileRows := [][]string{
		{"Name", report.User.Name},
		{"Username", "@" + report.User.Username},
		{"Email", report.User.Email},
		{"Role", formatRole(string(report.User.Role))},
		{"Status", formatActive(report.User.Active)},
		{"User ID", fmt.Sprintf("%d", report.User.UserID)},
	}
	keyValueTable(pdf, profileRows)

	sectionTitle(pdf, "Activity Summary", primary)
	summaryRows := [][]string{
		{"Projects (member)", fmt.Sprintf("%d", len(report.MemberProjects))},
		{"Projects (managed)", fmt.Sprintf("%d", len(report.ManagedProjects))},
		{"Total tasks assigned", fmt.Sprintf("%d", report.TotalTasks)},
		{"Pending tasks", fmt.Sprintf("%d", report.PendingTasks)},
		{"In progress tasks", fmt.Sprintf("%d", report.InProgressTasks)},
		{"Completed tasks", fmt.Sprintf("%d", report.CompletedTasks)},
		{"Blocked tasks", fmt.Sprintf("%d", report.BlockedTasks)},
		{"Avg. completion rate", fmt.Sprintf("%.1f%%", report.AvgCompletionRate)},
		{"Total estimated time", formatHours(report.TotalEstimated)},
		{"Total time on tasks", formatHours(report.TotalTaskTime)},
		{"Total logged time", formatHours(report.TotalEntryTime)},
		{"Time entries", fmt.Sprintf("%d", len(report.TimeEntries))},
	}
	keyValueTable(pdf, summaryRows)

	if len(report.MemberProjects) > 0 || len(report.ManagedProjects) > 0 {
		sectionTitle(pdf, "Projects", primary)
		projectHeaders := []string{"ID", "Name", "Status", "Role", "Start", "Est. End"}
		projectRows := make([][]string, 0)

		seen := map[int]bool{}
		for _, project := range report.ManagedProjects {
			if seen[project.ProjectID] {
				continue
			}
			seen[project.ProjectID] = true
			projectRows = append(projectRows, []string{
				fmt.Sprintf("%d", project.ProjectID),
				truncate(project.Name, 28),
				project.Status,
				"Manager",
				project.StartDate.Format("2006-01-02"),
				project.EstimatedEndDate.Format("2006-01-02"),
			})
		}
		for _, project := range report.MemberProjects {
			if seen[project.ProjectID] {
				continue
			}
			seen[project.ProjectID] = true
			projectRows = append(projectRows, []string{
				fmt.Sprintf("%d", project.ProjectID),
				truncate(project.Name, 28),
				project.Status,
				"Member",
				project.StartDate.Format("2006-01-02"),
				project.EstimatedEndDate.Format("2006-01-02"),
			})
		}
		dataTable(pdf, projectHeaders, projectRows)
	}

	if len(report.Tasks) > 0 {
		sectionTitle(pdf, "Tasks", primary)
		taskHeaders := []string{"ID", "Title", "Status", "Completion", "Spent", "Est."}
		taskRows := make([][]string, 0, len(report.Tasks))
		for _, task := range report.Tasks {
			taskRows = append(taskRows, []string{
				fmt.Sprintf("%d", task.TaskID),
				truncate(task.Title, 30),
				task.Status,
				fmt.Sprintf("%.0f%%", task.CompletionRate),
				formatHours(task.TimeSpent),
				formatHours(task.EstimatedTime),
			})
		}
		dataTable(pdf, taskHeaders, taskRows)
	}

	if len(report.TimeEntries) > 0 {
		sectionTitle(pdf, "Recent Time Entries", primary)
		entryHeaders := []string{"Date", "Task", "Hours", "Observation"}
		entryRows := make([][]string, 0)
		limit := len(report.TimeEntries)
		if limit > 25 {
			limit = 25
		}
		taskTitles := map[int]string{}
		for _, task := range report.Tasks {
			taskTitles[task.TaskID] = task.Title
		}
		for i := 0; i < limit; i++ {
			entry := report.TimeEntries[i]
			title := taskTitles[entry.TaskID]
			if title == "" {
				title = fmt.Sprintf("Task #%d", entry.TaskID)
			}
			entryRows = append(entryRows, []string{
				entry.WorkDate.Format("2006-01-02"),
				truncate(title, 24),
				formatHours(entry.TimeSpent),
				truncate(entry.Observation, 32),
			})
		}
		dataTable(pdf, entryHeaders, entryRows)
		if len(report.TimeEntries) > 25 {
			pdf.SetFont("Helvetica", "I", 8)
			pdf.SetTextColor(muted[0], muted[1], muted[2])
			pdf.CellFormat(0, 6, fmt.Sprintf("Showing 25 of %d time entries.", len(report.TimeEntries)), "", 1, "L", false, 0, "")
		}
	}

	var buffer bytes.Buffer
	if err := pdf.Output(&buffer); err != nil {
		return nil, err
	}

	return buffer.Bytes(), nil
}

func sectionTitle(pdf *fpdf.Fpdf, title string, color []int) {
	pdf.Ln(3)
	pdf.SetFont("Helvetica", "B", 12)
	pdf.SetTextColor(color[0], color[1], color[2])
	pdf.CellFormat(0, 8, title, "", 1, "L", false, 0, "")
	pdf.SetDrawColor(226, 232, 240)
	pdf.Line(15, pdf.GetY(), 195, pdf.GetY())
	pdf.Ln(2)
}

func keyValueTable(pdf *fpdf.Fpdf, rows [][]string) {
	pdf.SetFont("Helvetica", "", 9)
	for _, row := range rows {
		pdf.SetTextColor(100, 116, 139)
		pdf.CellFormat(52, 6, row[0], "", 0, "L", false, 0, "")
		pdf.SetTextColor(30, 41, 59)
		pdf.CellFormat(0, 6, row[1], "", 1, "L", false, 0, "")
	}
	pdf.Ln(2)
}

func dataTable(pdf *fpdf.Fpdf, headers []string, rows [][]string) {
	colWidths := distributeWidths(headers, 180.0)
	headerFill := []int{241, 245, 249}

	pdf.SetFont("Helvetica", "B", 8)
	pdf.SetFillColor(headerFill[0], headerFill[1], headerFill[2])
	pdf.SetTextColor(51, 65, 85)
	for i, header := range headers {
		pdf.CellFormat(colWidths[i], 7, header, "1", 0, "L", true, 0, "")
	}
	pdf.Ln(-1)

	pdf.SetFont("Helvetica", "", 8)
	fill := false
	for _, row := range rows {
		if pdf.GetY() > 270 {
			pdf.AddPage()
			pdf.SetFont("Helvetica", "B", 8)
			pdf.SetFillColor(headerFill[0], headerFill[1], headerFill[2])
			pdf.SetTextColor(51, 65, 85)
			for i, header := range headers {
				pdf.CellFormat(colWidths[i], 7, header, "1", 0, "L", true, 0, "")
			}
			pdf.Ln(-1)
			pdf.SetFont("Helvetica", "", 8)
		}

		if fill {
			pdf.SetFillColor(248, 250, 252)
		} else {
			pdf.SetFillColor(255, 255, 255)
		}
		pdf.SetTextColor(30, 41, 59)
		for i, cell := range row {
			pdf.CellFormat(colWidths[i], 6, cell, "1", 0, "L", true, 0, "")
		}
		pdf.Ln(-1)
		fill = !fill
	}
	pdf.Ln(2)
}

func distributeWidths(headers []string, total float64) []float64 {
	if len(headers) == 0 {
		return nil
	}
	weights := make([]float64, len(headers))
	for i, header := range headers {
		switch strings.ToLower(header) {
		case "id":
			weights[i] = 0.7
		case "status", "role", "hours", "spent", "est.", "completion":
			weights[i] = 1.0
		case "date", "start", "est. end":
			weights[i] = 1.2
		default:
			weights[i] = 2.0
		}
	}
	sum := 0.0
	for _, weight := range weights {
		sum += weight
	}
	widths := make([]float64, len(headers))
	for i, weight := range weights {
		widths[i] = total * weight / sum
	}
	return widths
}

func formatRole(role string) string {
	switch role {
	case string(entity.UserRoleAdmin):
		return "Administrator"
	case string(entity.UserRoleProjectManager):
		return "Project Manager"
	case string(entity.UserRoleUser):
		return "User"
	default:
		return role
	}
}

func formatActive(active bool) string {
	if active {
		return "Active"
	}
	return "Inactive"
}

func formatHours(hours float64) string {
	if math.Abs(hours) < 0.05 {
		return "0h"
	}
	if math.Mod(hours, 1) < 0.05 {
		return fmt.Sprintf("%.0fh", hours)
	}
	return fmt.Sprintf("%.1fh", hours)
}

func truncate(value string, max int) string {
	value = strings.TrimSpace(value)
	if len(value) <= max {
		return value
	}
	if max <= 3 {
		return value[:max]
	}
	return value[:max-3] + "..."
}

func sanitizeFilename(value string) string {
	value = strings.ToLower(strings.TrimSpace(value))
	var builder strings.Builder
	for _, r := range value {
		switch {
		case r >= 'a' && r <= 'z', r >= '0' && r <= '9':
			builder.WriteRune(r)
		case r == '-' || r == '_':
			builder.WriteRune(r)
		default:
			builder.WriteRune('-')
		}
	}
	return strings.Trim(builder.String(), "-")
}
