import React from 'react';
import {View, NativeModules} from 'react-native';
import ZoomMeeting from "./ZoomMeeting";

const {AwesomeZoomSDK} = NativeModules;

const App = () => {



  const ZOOM_CONFIG = {

  }

  return(
    <View style={{flex: 1}}>
      <ZoomMeeting/>
    </View>
  );
};

export default App;
