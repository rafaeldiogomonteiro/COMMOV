package entity

import "time"

type Project struct {
	ProjectID        int        `json:"projectId" gorm:"column:project_id;primaryKey;autoIncrement"`
	Name             string     `json:"name" gorm:"type:varchar(160);not null"`
	Description      string     `json:"description" gorm:"type:text"`
	Status           string     `json:"status" gorm:"type:varchar(50);not null"`
	ManagerID        int        `json:"managerId" gorm:"column:manager_id;not null"`
	CreatedBy        int        `json:"createdBy" gorm:"column:created_by;not null"`
	StartDate        time.Time  `json:"startDate" gorm:"column:start_date;type:date;not null"`
	EstimatedEndDate time.Time  `json:"estimatedEndDate" gorm:"column:estimated_end_date;type:date;not null"`
	ActualEndDate    *time.Time `json:"actualEndDate" gorm:"column:actual_end_date;type:date"`
}

func (Project) TableName() string {
	return "projects"
}
