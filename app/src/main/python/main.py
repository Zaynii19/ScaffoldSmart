import firebase_admin

from firebase_admin import credentials, messaging

cred = credentials.Certificate('firebase_cred.json')

firebase_admin.initialize_app(cred)

def send_fcm_notification(token, title, body):
    message = messaging.Message(
        notification = messaging.Notification (
        title = title,
        body = body,
        ),
        token = token
    )

    try:
        response = messaging.send(message)
        print('Notification sent successfully!', response)
    except Exception as e:
        print('Error sending notifications: ', e)

token = 'duzFgmMPQSacWRF8LhyK7q:APA91bHiTa3XULxqwroidYJvA9xHVw1R4KaaBKlTdG5EYQ2rKhAyICd4Oqu9P6HIFScZ4LRlTC8NwP0Q0l-tWtWJO4wmuPdGPxkElH8_ySd7ks5bft1xTt4'

send_fcm_notification(token, 'Test notification!', 'Hello free python firebase admin SDK')