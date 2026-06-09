package entity

import "time"

type TaskTimeEntry struct {
	EntryID     int       `json:"entryId" gorm:"column:entry_id;primaryKey;autoIncrement"`
	TaskID      int       `json:"taskId" gorm:"column:task_id;not null"`
	UserID      int       `json:"userId" gorm:"column:user_id;not null"`
	TimeSpent   float64   `json:"timeSpent" gorm:"column:time_spent;not null"`
	WorkDate    time.Time `json:"workDate" gorm:"column:work_date;type:date;not null"`
	Observation string    `json:"observation" gorm:"type:text;not null;default:''"`
	CreatedAt   time.Time `json:"createdAt" gorm:"column:created_at;not null;autoCreateTime"`
}

func (TaskTimeEntry) TableName() string {
	return "task_time_entries"
}

type TaskTimeEntryView struct {
	EntryID     int       `json:"entryId" gorm:"column:entry_id"`
	TaskID      int       `json:"taskId" gorm:"column:task_id"`
	UserID      int       `json:"userId" gorm:"column:user_id"`
	UserName    string    `json:"userName" gorm:"column:user_name"`
	UserPhoto   string    `json:"userPhoto" gorm:"column:user_photo"`
	TimeSpent   float64   `json:"timeSpent" gorm:"column:time_spent"`
	WorkDate    time.Time `json:"workDate" gorm:"column:work_date"`
	Observation string    `json:"observation" gorm:"column:observation"`
	CreatedAt   time.Time `json:"createdAt" gorm:"column:created_at"`
}
