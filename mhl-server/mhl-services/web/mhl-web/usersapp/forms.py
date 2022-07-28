from django.forms import ModelForm, DateInput, TimeInput
from usersapp.models import UserDetails

class DateInput(DateInput):
    input_type = 'date'
	
class TimeInput(TimeInput):
    input_type = 'time'

class UserModelForm(ModelForm):
    class Meta:
        model = UserDetails
        fields = '__all__'
        widgets = {
            'study_start_date': DateInput(),
			'study_end_date': DateInput()
        }
		
