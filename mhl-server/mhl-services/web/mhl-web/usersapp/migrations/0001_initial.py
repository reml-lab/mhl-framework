# Generated by Django 3.1.7 on 2021-03-25 00:30

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='UserDetails',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('study_name', models.CharField(choices=[('SCH-Amherst', 'SCH-Amherst'), ('SCH-UMMS', 'SCH-UMMS')], max_length=12)),
                ('config_version', models.IntegerField()),
                ('research_token', models.CharField(max_length=8)),
                ('badge_id', models.CharField(max_length=12)),
                ('study_start_date', models.DateField()),
                ('study_end_date', models.DateField()),
                ('home_longitude', models.DecimalField(decimal_places=6, max_digits=9)),
                ('home_latitude', models.DecimalField(decimal_places=6, max_digits=9)),
                ('collection_start_time', models.TimeField()),
                ('collection_end_time', models.TimeField()),
                ('duty_cycle_on_interval', models.IntegerField()),
                ('duty_cycle_off_interval', models.IntegerField()),
                ('num_self_report', models.IntegerField()),
            ],
        ),
    ]