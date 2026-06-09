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
