package entity

import "time"

type Task struct {
	TaskID           int        `json:"taskId" gorm:"column:task_id;primaryKey;autoIncrement"`
	ProjectID        int        `json:"projectId" gorm:"column:project_id;not null"`
	UserIDs          []int      `json:"userIds" gorm:"-"`
	Title            string     `json:"title" gorm:"type:varchar(160);not null"`
	Description      string     `json:"description" gorm:"type:text"`
	Status           string     `json:"status" gorm:"type:varchar(50);not null"`
	EstimatedEndDate *time.Time `json:"estimatedEndDate" gorm:"column:estimated_end_date;type:date"`
	ActualEndDate    *time.Time `json:"actualEndDate" gorm:"column:actual_end_date;type:date"`
	EstimatedTime    float64    `json:"estimatedTime" gorm:"column:estimated_time;not null;default:0"`
	TimeSpent        float64    `json:"timeSpent" gorm:"column:time_spent;not null;default:0"`
	CompletionRate   float64    `json:"completionRate" gorm:"column:completion_rate;not null;default:0"`
	WorkDate         *time.Time `json:"workDate" gorm:"column:work_date;type:date"`
	Location         string     `json:"location" gorm:"type:varchar(160)"`
	Observation      string     `json:"observation" gorm:"type:text"`
	Photo            string     `json:"photo" gorm:"type:text"`
}

func (Task) TableName() string {
	return "tasks"
}
