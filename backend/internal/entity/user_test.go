package entity

import "testing"

func TestIsValidUserRole(t *testing.T) {
	tests := []struct {
		role  UserRole
		valid bool
	}{
		{UserRoleAdmin, true},
		{UserRoleProjectManager, true},
		{UserRoleUser, true},
		{UserRole("guest"), false},
		{UserRole(""), false},
	}

	for _, test := range tests {
		if got := IsValidUserRole(test.role); got != test.valid {
			t.Fatalf("IsValidUserRole(%q) = %v, want %v", test.role, got, test.valid)
		}
	}
}
