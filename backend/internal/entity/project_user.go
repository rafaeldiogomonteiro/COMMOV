package entity

type ProjectUser struct {
	ProjectUserID int `json:"projectUserId" gorm:"column:project_user_id;primaryKey;autoIncrement"`
	ProjectID     int `json:"projectId" gorm:"column:project_id;not null"`
	UserID        int `json:"userId" gorm:"column:user_id;not null"`
}

func (ProjectUser) TableName() string {
	return "project_users"
}
