import json
from locust import HttpUser, task

class WebsiteUser(HttpUser):
    @task
    def index(self):
        self.client.post("/api/v1/orderservice/order/refresh",  data=json.dumps({
            "loginId": "4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f",
            "enableStateQuery": False,
            "enableTravelDateQuery": False,
            "enableBoughtDateQuery": False,
            "travelDateStart": None,
            "travelDateEnd": None,
            "boughtDateStart": None,
            "boughtDateEnd": None
        }), headers={'Content-Type':'application/json'})
    host = "http://39.104.62.233:32677"
    min_wait = 1000
    max_wait = 1000