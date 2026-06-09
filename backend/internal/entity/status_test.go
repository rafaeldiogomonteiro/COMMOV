package entity

import "testing"

func TestNormalizeProjectStatus(t *testing.T) {
	tests := []struct {
		input   string
		want    string
		wantOK  bool
	}{
		{"active", ProjectStatusActive, true},
		{"ACTIVE", ProjectStatusActive, true},
		{" completed ", ProjectStatusCompleted, true},
		{"on_hold", ProjectStatusOnHold, true},
		{"cancelled", ProjectStatusCancelled, true},
		{"", ProjectStatusActive, true},
		{"archived", "", false},
	}

	for _, test := range tests {
		got, ok := NormalizeProjectStatus(test.input)
		if ok != test.wantOK || got != test.want {
			t.Fatalf("NormalizeProjectStatus(%q) = (%q, %v), want (%q, %v)", test.input, got, ok, test.want, test.wantOK)
		}
	}
}

func TestIsValidProjectStatus(t *testing.T) {
	tests := []struct {
		status string
		valid  bool
	}{
		{"active", true},
		{" completed ", true},
		{"ON_HOLD", true},
		{"cancelled", true},
		{"archived", false},
	}

	for _, test := range tests {
		if got := IsValidProjectStatus(test.status); got != test.valid {
			t.Fatalf("IsValidProjectStatus(%q) = %v, want %v", test.status, got, test.valid)
		}
	}
}

func TestIsCompletedStatus(t *testing.T) {
	if !IsCompletedStatus("completed") || !IsCompletedStatus(" COMPLETED ") {
		t.Fatal("expected completed task status to be recognized")
	}
	if IsCompletedStatus("pending") {
		t.Fatal("expected non-completed status to return false")
	}
}

func TestNormalizeTaskStatus(t *testing.T) {
	tests := []struct {
		input  string
		want   string
		wantOK bool
	}{
		{"pending", TaskStatusPending, true},
		{"In_Progress", TaskStatusInProgress, true},
		{"completed", TaskStatusCompleted, true},
		{"blocked", TaskStatusBlocked, true},
		{"", TaskStatusPending, true},
		{"done", "", false},
	}

	for _, test := range tests {
		got, ok := NormalizeTaskStatus(test.input)
		if ok != test.wantOK || got != test.want {
			t.Fatalf("NormalizeTaskStatus(%q) = (%q, %v), want (%q, %v)", test.input, got, ok, test.want, test.wantOK)
		}
	}
}
