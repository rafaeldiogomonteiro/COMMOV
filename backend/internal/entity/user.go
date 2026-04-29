package entity

type User struct {
	UserID   int    `json:"userId" gorm:"column:user_id;primaryKey;autoIncrement"`
	Name     string `json:"name" gorm:"type:varchar(120);not null"`
	Username string `json:"username" gorm:"type:varchar(80);not null;uniqueIndex"`
	Email    string `json:"email" gorm:"type:varchar(160);not null;uniqueIndex"`
	Password string `json:"password" gorm:"type:text;not null"`
	Photo    string `json:"photo" gorm:"type:text"`
	Role     string `json:"role" gorm:"type:varchar(50);not null"`
	Active   bool   `json:"active" gorm:"not null;default:true"`
}

func (User) TableName() string {
	return "users"
}
