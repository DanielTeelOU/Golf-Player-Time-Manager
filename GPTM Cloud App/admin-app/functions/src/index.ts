// import * as functions from 'firebase-functions';
// import * as firebase from 'firebase/app';
// import 'firebase/database';

// // Delete all requests and games each morning
// export const scheduledFunctionCrontab =
// functions.pubsub.schedule('5 2 * * *').onRun((context) => {
//     // This will be run every day at 2:05 AM UTC
//     firebase.database().ref('Request').remove();
//     firebase.database().ref('Requests').remove();
//     firebase.database().ref('Games').remove();
// });
