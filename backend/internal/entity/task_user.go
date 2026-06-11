package entity

type TaskUser struct {
	TaskUserID int `json:"taskUserId" gorm:"column:task_user_id;primaryKey;autoIncrement"`
	TaskID     int `json:"taskId" gorm:"column:task_id;not null"`
	UserID     int `json:"userId" gorm:"column:user_id;not null"`
}

func (TaskUser) TableName() string {
	return "task_users"
}
