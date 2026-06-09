package services

import (
	"testing"
	"time"

	"commov/backend/internal/entity"
)

func TestNormalizeProjectText(t *testing.T) {
	tests := []struct {
		name        string
		inputName   string
		description string
		status      string
		wantName    string
		wantDesc    string
		wantStatus  string
		wantErr     bool
	}{
		{
			name:       "trims and normalizes status",
			inputName:  "  Site A  ",
			description: "  desc  ",
			status:     " COMPLETED ",
			wantName:   "Site A",
			wantDesc:   "desc",
			wantStatus: entity.ProjectStatusCompleted,
		},
		{
			name:       "defaults empty status to active",
			inputName:  "Site B",
			description: "",
			status:     "",
			wantName:   "Site B",
			wantDesc:   "",
			wantStatus: entity.ProjectStatusActive,
		},
		{
			name:      "rejects empty name",
			inputName: "   ",
			wantErr:   true,
		},
		{
			name:      "rejects invalid status",
			inputName: "Site C",
			status:    "archived",
			wantErr:   true,
		},
	}

	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			gotName, gotDesc, gotStatus, err := normalizeProjectText(test.inputName, test.description, test.status)
			if test.wantErr {
				if err == nil {
					t.Fatal("expected error, got nil")
				}
				return
			}
			if err != nil {
				t.Fatalf("unexpected error: %v", err)
			}
			if gotName != test.wantName || gotDesc != test.wantDesc || gotStatus != test.wantStatus {
				t.Fatalf("normalizeProjectText() = (%q, %q, %q), want (%q, %q, %q)",
					gotName, gotDesc, gotStatus, test.wantName, test.wantDesc, test.wantStatus)
			}
		})
	}
}

func TestValidateProjectDates(t *testing.T) {
	start := time.Date(2026, 6, 1, 0, 0, 0, 0, time.UTC)
	estimated := time.Date(2026, 6, 30, 0, 0, 0, 0, time.UTC)
	actual := time.Date(2026, 6, 15, 0, 0, 0, 0, time.UTC)

	if err := validateProjectDates(start, estimated, &actual); err != nil {
		t.Fatalf("expected valid dates, got %v", err)
	}

	if err := validateProjectDates(time.Time{}, estimated, nil); err == nil {
		t.Fatal("expected missing startDate to fail")
	}

	if err := validateProjectDates(start, time.Time{}, nil); err == nil {
		t.Fatal("expected missing estimatedEndDate to fail")
	}

	if err := validateProjectDates(start, start.Add(-24*time.Hour), nil); err == nil {
		t.Fatal("expected estimatedEndDate before startDate to fail")
	}
}

func TestUniquePositiveIDs(t *testing.T) {
	got := uniquePositiveIDs([]int{3, 1, 3, 0, -2, 1, 2})
	want := []int{3, 1, 2}

	if len(got) != len(want) {
		t.Fatalf("uniquePositiveIDs() = %v, want %v", got, want)
	}
	for i := range want {
		if got[i] != want[i] {
			t.Fatalf("uniquePositiveIDs() = %v, want %v", got, want)
		}
	}
}

func TestCompletionDateForProject(t *testing.T) {
	futureStart := currentDate().AddDate(0, 0, 10)
	got := completionDateForProject(futureStart)
	if !got.Equal(dateOnly(futureStart)) {
		t.Fatalf("completionDateForProject(future) = %v, want %v", got, dateOnly(futureStart))
	}

	pastStart := currentDate().AddDate(0, 0, -10)
	got = completionDateForProject(pastStart)
	if !got.Equal(currentDate()) {
		t.Fatalf("completionDateForProject(past) = %v, want %v", got, currentDate())
	}
}
