from django.shortcuts import render
from django.db import models
from django.template import loader
from django.http import HttpResponse
from django.forms import modelformset_factory
from django.views.generic import ListView, FormView, UpdateView, DeleteView, TemplateView

# Create your views here.

from .forms import UserModelForm
from .models import UserDetails


class UserVis(TemplateView):

    template_name = 'user_vis.html'

class UserList(ListView):
    model = UserDetails
    template_name = 'user_list.html'

class UserCreate(FormView):
    template_name = 'user_form.html'
    model = UserDetails
    form_class = UserModelForm
    success_url = '/user/list/'
	
    def form_valid(self, form):
        form.save()
        return super().form_valid(form)

class UserEdit(UpdateView):
    template_name = 'user_form.html'
    form_class = UserModelForm
    model = UserDetails
    success_url = '/user/list/'

    def form_valid(self, form):
       form.save()
       return super().form_valid(form)   
       
class UserDelete(DeleteView):
    template_name = 'user_delete.html'
    model = UserDetails
    success_url = '/user/list/'
 