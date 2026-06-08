package services

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"

	"commov/backend/internal/entity"
	"commov/backend/internal/postgres"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

const defaultTokenTTL = 7 * 24 * time.Hour

type authSession struct {
	userID    int
	expiresAt time.Time
}

type AuthService struct {
	UserRepo *postgres.UserRepo
	tokens   map[string]authSession
	mu       sync.RWMutex
	tokenTTL time.Duration
}

func (s *AuthService) Login(ctx context.Context, email string, password string) (string, *entity.User, error) {
	email = strings.ToLower(strings.TrimSpace(email))
	if email == "" || password == "" {
		return "", nil, validationError("email and password are required")
	}

	user, err := s.UserRepo.GetByEmail(ctx, email)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return "", nil, fmt.Errorf("%w: invalid credentials", ErrUnauthorized)
		}
		return "", nil, fmt.Errorf("get user by email: %w", err)
	}
	if !user.Active {
		return "", nil, fmt.Errorf("%w: user is inactive", ErrUnauthorized)
	}
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password)); err != nil {
		return "", nil, fmt.Errorf("%w: invalid credentials", ErrUnauthorized)
	}

	token, err := newToken()
	if err != nil {
		return "", nil, fmt.Errorf("generate token: %w", err)
	}

	s.mu.Lock()
	if s.tokens == nil {
		s.tokens = make(map[string]authSession)
	}
	s.tokens[token] = authSession{
		userID:    user.UserID,
		expiresAt: time.Now().UTC().Add(s.tokenTTLDuration()),
	}
	s.mu.Unlock()

	return token, user, nil
}

func (s *AuthService) Logout(token string) {
	token = strings.TrimSpace(token)
	if token == "" {
		return
	}

	s.mu.Lock()
	delete(s.tokens, token)
	s.mu.Unlock()
}

func (s *AuthService) RevokeUserTokens(userID int) {
	if userID <= 0 {
		return
	}

	s.mu.Lock()
	for token, session := range s.tokens {
		if session.userID == userID {
			delete(s.tokens, token)
		}
	}
	s.mu.Unlock()
}

func (s *AuthService) CheckLogin(ctx context.Context, token string) (*entity.User, bool, error) {
	token = strings.TrimSpace(token)
	if token == "" {
		return nil, false, nil
	}

	userID, ok := s.lookupToken(token)
	if !ok {
		return nil, false, nil
	}

	user, err := s.UserRepo.GetByID(ctx, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			s.Logout(token)
			return nil, false, nil
		}
		return nil, false, fmt.Errorf("get user by token: %w", err)
	}
	if !user.Active {
		s.Logout(token)
		return nil, false, nil
	}

	return user, true, nil
}

func (s *AuthService) lookupToken(token string) (int, bool) {
	now := time.Now().UTC()

	s.mu.Lock()
	defer s.mu.Unlock()

	session, exists := s.tokens[token]
	if !exists {
		return 0, false
	}
	if !session.expiresAt.After(now) {
		delete(s.tokens, token)
		return 0, false
	}

	return session.userID, true
}

func (s *AuthService) tokenTTLDuration() time.Duration {
	if s.tokenTTL > 0 {
		return s.tokenTTL
	}
	return defaultTokenTTL
}

func newToken() (string, error) {
	randomBytes := make([]byte, 32)
	if _, err := rand.Read(randomBytes); err != nil {
		return "", err
	}

	return hex.EncodeToString(randomBytes), nil
}
