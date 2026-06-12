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
	ProjectUserRepo   *postgres.ProjectUserRepo
	TaskRepo          *postgres.TaskRepo
	TaskUserRepo      *postgres.TaskUserRepo
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
	TodoTasks         int
	CompletedTasks    int
	TotalTaskTime     float64
	TotalEntryTime    float64
	TotalEstimated    float64
	AvgCompletionRate float64
}

type ProjectReport struct {
	Project           entity.Project
	GeneratedAt       time.Time
	Manager           *entity.User
	Members           []entity.User
	Tasks             []entity.Task
	TotalTasks        int
	TodoTasks         int
	CompletedTasks    int
	TotalTaskTime     float64
	TotalEstimated    float64
	AvgCompletionRate float64
}

func (s *UserReportService) ListUsers(ctx context.Context, actorUserID int) ([]entity.User, error) {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return nil, err
	}

	users, err := s.UserRepo.List(ctx)
	if err != nil {
		return nil, fmt.Errorf("list users: %w", err)
	}

	return users, nil
}

func (s *UserReportService) BuildReport(ctx context.Context, actorUserID int, userID int) (*UserReport, error) {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
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
		status, _ := entity.NormalizeTaskStatus(task.Status)
		switch status {
		case entity.TaskStatusCompleted:
			report.CompletedTasks++
		default:
			report.TodoTasks++
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

func (s *UserReportService) BuildProjectReport(ctx context.Context, actorUserID int, projectID int) (*ProjectReport, error) {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return nil, err
	}
	if projectID <= 0 {
		return nil, validationError("projectId is invalid")
	}

	project, err := s.ProjectRepo.GetByID(ctx, projectID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError("project not found")
		}
		return nil, fmt.Errorf("get project: %w", err)
	}

	manager, err := s.UserRepo.GetByID(ctx, project.ManagerID)
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, fmt.Errorf("get manager: %w", err)
	}

	projectUsers, err := s.ProjectUserRepo.ListByProjectID(ctx, projectID)
	if err != nil {
		return nil, fmt.Errorf("list project members: %w", err)
	}

	members := make([]entity.User, 0, len(projectUsers))
	for _, projectUser := range projectUsers {
		user, err := s.UserRepo.GetByID(ctx, projectUser.UserID)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				continue
			}
			return nil, fmt.Errorf("get project member: %w", err)
		}
		members = append(members, *user)
	}

	tasks, err := s.TaskRepo.ListByProjectID(ctx, projectID)
	if err != nil {
		return nil, fmt.Errorf("list project tasks: %w", err)
	}

	report := &ProjectReport{
		Project:     *project,
		GeneratedAt: time.Now().UTC(),
		Manager:     manager,
		Members:     members,
		Tasks:       tasks,
		TotalTasks:  len(tasks),
	}

	for _, task := range tasks {
		status, _ := entity.NormalizeTaskStatus(task.Status)
		switch status {
		case entity.TaskStatusCompleted:
			report.CompletedTasks++
		default:
			report.TodoTasks++
		}
		report.TotalTaskTime += task.TimeSpent
		report.TotalEstimated += task.EstimatedTime
		report.AvgCompletionRate += task.CompletionRate
	}

	if report.TotalTasks > 0 {
		report.AvgCompletionRate = report.AvgCompletionRate / float64(report.TotalTasks)
	}

	return report, nil
}

func (s *UserReportService) ExportProjectPDF(ctx context.Context, actorUserID int, projectID int) ([]byte, string, error) {
	report, err := s.BuildProjectReport(ctx, actorUserID, projectID)
	if err != nil {
		return nil, "", err
	}

	pdfBytes, err := renderProjectReportPDF(report)
	if err != nil {
		return nil, "", fmt.Errorf("render pdf: %w", err)
	}

	safeName := sanitizeFilename(report.Project.Name)
	if safeName == "" {
		safeName = fmt.Sprintf("project-%d", report.Project.ProjectID)
	}
	filename := fmt.Sprintf("commov-project-%s-report.pdf", safeName)

	return pdfBytes, filename, nil
}

func (s *UserReportService) ExportProjectTasksPDF(ctx context.Context, actorUserID int, projectID int) ([]byte, string, error) {
	report, err := s.BuildProjectReport(ctx, actorUserID, projectID)
	if err != nil {
		return nil, "", err
	}

	pdfBytes, err := renderProjectTasksReportPDF(report, s.UserRepo, s.TaskUserRepo, ctx)
	if err != nil {
		return nil, "", fmt.Errorf("render pdf: %w", err)
	}

	safeName := sanitizeFilename(report.Project.Name)
	if safeName == "" {
		safeName = fmt.Sprintf("project-%d", report.Project.ProjectID)
	}
	filename := fmt.Sprintf("commov-project-%s-tasks-report.pdf", safeName)

	return pdfBytes, filename, nil
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
		{"Todo tasks", fmt.Sprintf("%d", report.TodoTasks)},
		{"Completed tasks", fmt.Sprintf("%d", report.CompletedTasks)},
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

func renderProjectReportPDF(report *ProjectReport) ([]byte, error) {
	pdf := fpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(15, 15, 15)
	pdf.SetAutoPageBreak(true, 15)
	pdf.AddPage()

	primary := []int{37, 99, 235}
	muted := []int{100, 116, 139}

	pdf.SetFont("Helvetica", "B", 20)
	pdf.SetTextColor(primary[0], primary[1], primary[2])
	pdf.CellFormat(0, 12, "ComMov Project Statistics Report", "", 1, "L", false, 0, "")

	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(muted[0], muted[1], muted[2])
	pdf.CellFormat(0, 6, fmt.Sprintf("Generated: %s UTC", report.GeneratedAt.Format("2006-01-02 15:04")), "", 1, "L", false, 0, "")
	pdf.Ln(4)

	sectionTitle(pdf, "Project", primary)
	managerName := "—"
	if report.Manager != nil {
		managerName = report.Manager.Name
	}
	actualEnd := "—"
	if report.Project.ActualEndDate != nil {
		actualEnd = report.Project.ActualEndDate.Format("2006-01-02")
	}
	projectRows := [][]string{
		{"Name", report.Project.Name},
		{"Description", truncate(report.Project.Description, 120)},
		{"Status", report.Project.Status},
		{"Manager", managerName},
		{"Project ID", fmt.Sprintf("%d", report.Project.ProjectID)},
		{"Start date", report.Project.StartDate.Format("2006-01-02")},
		{"Estimated end", report.Project.EstimatedEndDate.Format("2006-01-02")},
		{"Actual end", actualEnd},
	}
	keyValueTable(pdf, projectRows)

	sectionTitle(pdf, "Task Summary", primary)
	summaryRows := [][]string{
		{"Total tasks", fmt.Sprintf("%d", report.TotalTasks)},
		{"Todo tasks", fmt.Sprintf("%d", report.TodoTasks)},
		{"Completed tasks", fmt.Sprintf("%d", report.CompletedTasks)},
		{"Avg. completion rate", fmt.Sprintf("%.1f%%", report.AvgCompletionRate)},
		{"Total estimated time", formatHours(report.TotalEstimated)},
		{"Total time spent", formatHours(report.TotalTaskTime)},
		{"Members", fmt.Sprintf("%d", len(report.Members))},
	}
	keyValueTable(pdf, summaryRows)

	if len(report.Members) > 0 {
		sectionTitle(pdf, "Members", primary)
		memberHeaders := []string{"ID", "Name", "Role", "Status"}
		memberRows := make([][]string, 0, len(report.Members))
		for _, member := range report.Members {
			memberRows = append(memberRows, []string{
				fmt.Sprintf("%d", member.UserID),
				truncate(member.Name, 28),
				formatRole(string(member.Role)),
				formatActive(member.Active),
			})
		}
		dataTable(pdf, memberHeaders, memberRows)
	}

	if len(report.Tasks) > 0 {
		sectionTitle(pdf, "Tasks Overview", primary)
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

	var buffer bytes.Buffer
	if err := pdf.Output(&buffer); err != nil {
		return nil, err
	}

	return buffer.Bytes(), nil
}

func renderProjectTasksReportPDF(
	report *ProjectReport,
	userRepo *postgres.UserRepo,
	taskUserRepo *postgres.TaskUserRepo,
	ctx context.Context,
) ([]byte, error) {
	pdf := fpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(15, 15, 15)
	pdf.SetAutoPageBreak(true, 15)
	pdf.AddPage()

	primary := []int{37, 99, 235}
	muted := []int{100, 116, 139}

	pdf.SetFont("Helvetica", "B", 20)
	pdf.SetTextColor(primary[0], primary[1], primary[2])
	pdf.CellFormat(0, 12, "ComMov Project Tasks Report", "", 1, "L", false, 0, "")

	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(muted[0], muted[1], muted[2])
	pdf.CellFormat(0, 6, fmt.Sprintf("Project: %s", report.Project.Name), "", 1, "L", false, 0, "")
	pdf.CellFormat(0, 6, fmt.Sprintf("Generated: %s UTC", report.GeneratedAt.Format("2006-01-02 15:04")), "", 1, "L", false, 0, "")
	pdf.Ln(4)

	sectionTitle(pdf, "Summary", primary)
	summaryRows := [][]string{
		{"Total tasks", fmt.Sprintf("%d", report.TotalTasks)},
		{"Todo", fmt.Sprintf("%d", report.TodoTasks)},
		{"Completed", fmt.Sprintf("%d", report.CompletedTasks)},
		{"Total estimated time", formatHours(report.TotalEstimated)},
		{"Total time spent", formatHours(report.TotalTaskTime)},
	}
	keyValueTable(pdf, summaryRows)

	if len(report.Tasks) == 0 {
		sectionTitle(pdf, "Tasks", primary)
		pdf.SetFont("Helvetica", "", 9)
		pdf.SetTextColor(muted[0], muted[1], muted[2])
		pdf.CellFormat(0, 6, "No tasks found for this project.", "", 1, "L", false, 0, "")
	} else {
		assigneeNames := map[int]string{}
		taskAssigneeLabels := make(map[int]string, len(report.Tasks))
		for _, task := range report.Tasks {
			assigneeIDs, err := taskUserRepo.ListUserIDsByTaskID(ctx, task.TaskID)
			if err != nil {
				return nil, fmt.Errorf("list task assignees: %w", err)
			}
			names := make([]string, 0, len(assigneeIDs))
			for _, assigneeID := range assigneeIDs {
				name, ok := assigneeNames[assigneeID]
				if !ok {
					user, err := userRepo.GetByID(ctx, assigneeID)
					if err != nil || user == nil {
						name = fmt.Sprintf("User #%d", assigneeID)
					} else {
						name = user.Name
					}
					assigneeNames[assigneeID] = name
				}
				names = append(names, name)
			}
			taskAssigneeLabels[task.TaskID] = strings.Join(names, ", ")
		}

		sectionTitle(pdf, "Tasks", primary)
		taskHeaders := []string{"ID", "Title", "Assignees", "Status", "Completion", "Spent", "Est."}
		taskRows := make([][]string, 0, len(report.Tasks))
		for _, task := range report.Tasks {
			taskRows = append(taskRows, []string{
				fmt.Sprintf("%d", task.TaskID),
				truncate(task.Title, 24),
				truncate(taskAssigneeLabels[task.TaskID], 18),
				task.Status,
				fmt.Sprintf("%.0f%%", task.CompletionRate),
				formatHours(task.TimeSpent),
				formatHours(task.EstimatedTime),
			})
		}
		dataTable(pdf, taskHeaders, taskRows)
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
