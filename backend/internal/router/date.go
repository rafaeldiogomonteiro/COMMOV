package router

import (
	"fmt"
	"strings"
	"time"
)

const dateLayout = "2006-01-02"

func parseRequiredDate(value string, fieldName string) (time.Time, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return time.Time{}, fmt.Errorf("%s is required", fieldName)
	}

	parsed, err := time.Parse(dateLayout, value)
	if err != nil {
		return time.Time{}, fmt.Errorf("%s must use YYYY-MM-DD", fieldName)
	}

	return parsed, nil
}

func parseOptionalDate(value string, fieldName string) (*time.Time, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return nil, nil
	}

	parsed, err := time.Parse(dateLayout, value)
	if err != nil {
		return nil, fmt.Errorf("%s must use YYYY-MM-DD", fieldName)
	}

	return &parsed, nil
}

func parseDatePointer(value *string, fieldName string) (*time.Time, error) {
	if value == nil {
		return nil, nil
	}

	return parseOptionalDate(*value, fieldName)
}
