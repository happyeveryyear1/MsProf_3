from click import argument
from locust import HttpUser, TaskSet, task,  events
import requests
import json



class WebsiteUser(HttpUser):



    @task
    def about(self):
        self.client.get("/")

    @task
    def about(self):
        self.client.get("/")
        
    
    host = "http://www.baidu.com"
    min_wait = 1000
    max_wait = 5000

