package services

import (
	"testing"
	"time"
)

func TestCurrentDateUsesUTC(t *testing.T) {
	got := currentDate()
	if got.Location() != time.UTC {
		t.Fatalf("currentDate() location = %v, want UTC", got.Location())
	}
	if got.Hour() != 0 || got.Minute() != 0 || got.Second() != 0 {
		t.Fatalf("currentDate() = %v, expected midnight UTC", got)
	}
}

func TestDateOnlyComparison(t *testing.T) {
	left := time.Date(2026, 6, 4, 23, 0, 0, 0, time.FixedZone("Lisbon", 3600))
	right := time.Date(2026, 6, 4, 1, 0, 0, 0, time.UTC)

	if dateBefore(left, right) || dateAfter(left, right) {
		t.Fatal("expected same calendar day to compare as equal")
	}
}
