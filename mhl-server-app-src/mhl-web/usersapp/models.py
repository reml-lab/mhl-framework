from django.db import models

class UserDetails(models.Model):
    
    STUDY_CHOICES =[("MHL-Demo","MHL-Demo")]
    
    study_name              = models.CharField(max_length=12,choices=STUDY_CHOICES)
    config_version          = models.IntegerField()
    research_token          = models.CharField(max_length=8)
    badge_id                = models.CharField(max_length=12)
    study_start_date        = models.DateField()
    study_end_date          = models.DateField()
        
    def __str__(self):
        return self.badge_id
