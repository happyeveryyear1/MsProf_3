
from locust import HttpUser, task
import json


# 请求列表
test_list = ['{ "path": "/api/v1/travelservice/trips/left","method": "POST","argument": {"startingPlace":"Xu Zhou2","endPlace":"Xu Zhou2","departureTime":"2017-00-08"},"argInUrl": "false"}',
'{ "path": "/api/v1/verifycode/generate","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/verifycode/generate","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/travelservice/trips/left","method": "POST","argument": {"startingPlace":"Wu Xi2","endPlace":"Su Zhou","departureTime":"2022-06-22"},"argInUrl": "false"}',
'{ "path": "/client_ticket_book.html?tripId=D1345&from=Shang%20Hai&to=Su%20Zhou&seatType=2&seat_price=50.0&date=2022-06-22","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/assuranceservice/assurances/types","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/verifycode/generate","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/users/login","method": "POST","argument": {"username":"yfmldervssyovdjue","password":"111111","verificationCode":0},"argInUrl": "false"}',
'{ "path": "/api/v1/verifycode/generate","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/travelservice/trips/left","method": "POST","argument": {"startingPlace":"Shang Hai","endPlace":"Su Zhou","departureTime":"2022-06-22"},"argInUrl": "false"}',
'{ "path": "/api/v1/assuranceservice/assurances/types","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/foodservice/foods/2022-06-22/Shang%20Hai/Su%20Zhou/D1345","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/contactservice/contacts/account/4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/preserveservice/preserve","method": "POST","argument": {"accountId":"4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f","contactsId":"f0ba1dd0-adae-4c9e-b198-a957a2b59db6","tripId":"D1345","seatType":"2","date":"2022-06-22","from":"Shang Hai","to":"Su Zhou","assurance":"0","foodType":1,"foodName":"Bone Soup","foodPrice":2.5,"stationName":"","storeName":""},"argInUrl": "false"}',
'{ "path": "/api/v1/preserveservice/preserve","method": "POST","argument": {"accountId":"s4zobr7mxydr1glrsckyu0bqwat","contactsId":"l7rin3jpgyltnbtag9cj-pksx-tdla2kmlha5cq","tripId":"D1345","seatType":"2","date":"2024-07-25","from":"Shang Hai","to":"Su Zhou","assurance":"0","foodType":"","foodName":"Bone Soup","foodPrice":"","stationName":"","storeName":"","handleDate":"2000-03-10","consigneeName":"11","consigneePhone":"11111","consigneeWeight":"","isWithin":""},"argInUrl": "false"}',
'{ "path": "/client_ticket_book.html?tripId=D1345&from=Shang%20Hai&to=Su%20Zhou&seatType=2&seat_price=50.0&date=2022-06-22","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/assuranceservice/assurances/types","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/foodservice/foods/2022-06-22/Shang%20Hai/Su%20Zhou/D1345","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/contactservice/contacts/account/4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/preserveservice/preserve","method": "POST","argument": {"accountId":"4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f","contactsId":"st7i0s6d-qaevrip9flcpwbfvvdy3escu3cmbm4","tripId":"ow1q4","seatType":"","date":"2002-10-27","from":"fthh","to":"rzsuo","assurance":-18,"foodType":"","foodName":"Bone Soup","foodPrice":"","stationName":"","storeName":"","handleDate":"2002-06-04","consigneeName":"","consigneePhone":"","consigneeWeight":"","isWithin":""},"argInUrl": "false"}',
'{ "path": "/api/v1/preserveservice/preserve","method": "POST","argument": {"accountId":"4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f","contactsId":"4700346f-afb8-490c-b2fe-d32dc35b458f","tripId":"tDgr5","seatType":0,"date":"2003-01-24","from":"mqaezs","to":"Su Zhou","assurance":-4,"foodType":"","foodName":"Bone Soup","foodPrice":"","stationName":"","storeName":"","handleDate":"2022-06-22","consigneeName":"","consigneePhone":"","consigneeWeight":"","isWithin":""},"argInUrl": "false"}',
'{ "path": "/api/v1/consignservice/consigns/account/4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/verifycode/generate","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/verifycode/generate","method": "GET","argument": {},"argInUrl": "false"}',
'{ "path": "/api/v1/travelservice/trips/left","method": "POST","argument": {"startingPlace":"Nan Jing2","endPlace":"Xu Zhou2","departureTime":"2001-07-09"},"argInUrl": "false"}']

class WebsiteUser(HttpUser):

    @task
    def about(self):
        for test in test_list:
            test_json = json.loads(test)
            path = test_json['path']
            argument = test_json['argument']
            method = test_json['method']
            header = {'my_token': 1, 'Task_ID': 2, 'Test_ID': 3}
            if method == 'POST':
                self.client.post(path, json=argument, headers = header)
            else:
                self.client.get(path)
        
    
    host = "http://133.133.135.182:32677"
    min_wait = 1000
    max_wait = 5000

