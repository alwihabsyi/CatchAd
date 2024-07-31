/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotificationOnNewContent = functions.firestore
    .document('Contents/{contentId}')
    .onCreate((_snap, context) => {
        const contentId = context.params.contentId;

        const payload = {
            notification: {
                title: 'Konten Baru Ditambahkan',
                body: `Terdapat konten baru untuk device ${contentId}`,
            }
        };

        return admin.messaging().sendToTopic('new-content', payload)
            .then(response => {
                console.log('Notification sent successfully:', response);
                return null;
            })
            .catch(error => {
                console.log('Error sending notification:', error);
                throw new Error('Error sending notification');
            });
    });
