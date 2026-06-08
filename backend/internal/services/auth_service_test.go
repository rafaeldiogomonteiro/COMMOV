package services

import (
	"testing"
	"time"
)

func TestAuthServiceTokenExpiry(t *testing.T) {
	auth := &AuthService{
		tokens: map[string]authSession{
			"expired-token": {
				userID:    1,
				expiresAt: time.Now().UTC().Add(-time.Hour),
			},
			"valid-token": {
				userID:    2,
				expiresAt: time.Now().UTC().Add(time.Hour),
			},
		},
	}

	if userID, ok := auth.lookupToken("expired-token"); ok || userID != 0 {
		t.Fatalf("expected expired token to be rejected, got userID=%d ok=%v", userID, ok)
	}

	userID, ok := auth.lookupToken("valid-token")
	if !ok || userID != 2 {
		t.Fatalf("lookupToken(valid-token) = (%d, %v), want (2, true)", userID, ok)
	}
}

func TestAuthServiceRevokeUserTokens(t *testing.T) {
	auth := &AuthService{
		tokens: map[string]authSession{
			"token-a": {userID: 1, expiresAt: time.Now().UTC().Add(time.Hour)},
			"token-b": {userID: 2, expiresAt: time.Now().UTC().Add(time.Hour)},
			"token-c": {userID: 1, expiresAt: time.Now().UTC().Add(time.Hour)},
		},
	}

	auth.RevokeUserTokens(1)

	if _, ok := auth.lookupToken("token-a"); ok {
		t.Fatal("expected token-a to be revoked")
	}
	if _, ok := auth.lookupToken("token-c"); ok {
		t.Fatal("expected token-c to be revoked")
	}
	if userID, ok := auth.lookupToken("token-b"); !ok || userID != 2 {
		t.Fatalf("expected token-b to remain valid, got (%d, %v)", userID, ok)
	}
}
