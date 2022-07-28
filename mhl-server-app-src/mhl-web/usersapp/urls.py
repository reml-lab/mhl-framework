from django.urls import path
from django.contrib import admin

from usersapp.views import UserList, UserCreate, UserEdit, UserDelete, UserVis

urlpatterns = [
 path('user/create/', UserCreate.as_view()),
 path('user/list/', UserList.as_view()),
 path('user/<int:pk>/edit/', UserEdit.as_view()),
 path('user/<int:pk>/delete/', UserDelete.as_view()),
 path('user/<str:badge_id>/vis/', UserVis.as_view()),
]
