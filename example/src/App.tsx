import React, { useEffect } from 'react'
import RNBeaconScannerModule, { Counter } from 'react-native-beacon-scanner'

const App = () => {
  useEffect(() => {
    console.log(RNBeaconScannerModule)
  })

  return <Counter />
}

export default App
