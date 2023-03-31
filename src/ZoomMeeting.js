import { NativeModules, DeviceEventEmitter, NativeEventEmitter } from "react-native";
import React, {useEffect, useState} from 'react';
import {Button, Alert} from 'react-native';
const {ZoomManager: AwesomeZoomSDK} = NativeModules;

//to see what is loaded
console.log(NativeModules);

async function initZoom(publicKey, privateKey, domain) {
  console.log('calling zoom', AwesomeZoomSDK);
  const response = await AwesomeZoomSDK.initZoom(publicKey, privateKey, domain);

  console.log('Response', response);
  return response;
}

async function joinMeeting(displayName, meetingNo, password) {
  console.log('calling zoom - join meeting', displayName, meetingNo, password);
  const response = await AwesomeZoomSDK.joinMeeting(
    displayName,
    meetingNo,
    password,
  );

  console.log('Response - Join Meeting', response);
  return response;
}

async function startMeeting(
  meetingNumber,
  username,
  userId,
  jwtAccessToken,
  jwtApiKey,
) {
  console.log(
    'calling zoom',
    meetingNumber,
    username,
    userId,
    jwtAccessToken,
    jwtApiKey,
  );
  const response = await AwesomeZoomSDK.startMeeting(
    meetingNumber,
    username,
    userId,
    jwtAccessToken,
    jwtApiKey,
  );

  console.log('Response - Start Meeting', response);
}

const CLIENT_KEY = '97x7L4HzSwir5ZQwvI66sQ';
const CLIENT_SECRET = '0KAHbpeGffCVboQKNxDv2jGSscFePRnV';

const ZoomMeeting = () => {
  const [isInitialized, setIsInitialized] = useState(false);
  useEffect(() => {
    (async () => {
      if (!isInitialized) {
        try {
          const message = await initZoom(
            CLIENT_KEY,
            CLIENT_SECRET,
            'https://us05web.zoom.us',
          );
          console.log('message is ', message);
          setIsInitialized(true);
        } catch (error) {
          Alert.alert('error is ', error.toString());
        }
      }
    })();

    // const eventEmitter = new NativeEventEmitter(AwesomeZoomSDK);
    // this.eventListener = eventEmitter.addListener('EventReminder', event => {
    //   console.log(event.eventProperty) // "someValue"
    // });


  }, []);

  DeviceEventEmitter.addListener('EventReminder', (val) => {
    console.log('Native Zoom SDK saying:', val);
  });

  const joinMeetingFunc = async () => {
    try {
      const userName = 'Gaurav Rana';
      const meetingNumber = '97369793408';
      const password = 'X4HaKw';
      await joinMeeting(userName, meetingNumber, password);
    } catch (error) {
      Alert.alert('error is ', error.toString());
    }
  };

  return (
    <Button
      title={'Join Meeting'}
      disabled={!isInitialized}
      onPress={joinMeetingFunc}
    />
  );
};

export default ZoomMeeting;

export {initZoom, joinMeeting, startMeeting};
