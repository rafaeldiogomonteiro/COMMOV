package entity

type UserRole string

const (
	UserRoleAdmin          UserRole = "admin"
	UserRoleProjectManager UserRole = "project_manager"
	UserRoleUser           UserRole = "user"
)

func IsValidUserRole(role UserRole) bool {
	switch role {
	case UserRoleAdmin, UserRoleProjectManager, UserRoleUser:
		return true
	default:
		return false
	}
}

type User struct {
	UserID   int      `json:"userId" gorm:"column:user_id;primaryKey;autoIncrement"`
	Name     string   `json:"name" gorm:"type:varchar(120);not null"`
	Username string   `json:"username" gorm:"type:varchar(80);not null;uniqueIndex"`
	Email    string   `json:"email" gorm:"type:varchar(160);not null;uniqueIndex"`
	Password string   `json:"-" gorm:"type:text;not null"`
	Photo    string   `json:"photo" gorm:"type:text"`
	Role     UserRole `json:"role" gorm:"type:user_role;not null;default:user"`
	Active   bool     `json:"active" gorm:"not null;default:true"`
}

func (User) TableName() string {
	return "users"
}
