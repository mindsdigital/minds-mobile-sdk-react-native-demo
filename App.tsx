import React, {useCallback, useMemo, useRef, useState} from 'react';
import {Button, Image, StyleSheet, Text, TextInput, View} from 'react-native';

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scroll-view';
import Icon from 'react-native-vector-icons/MaterialIcons';
import {BottomSheetModal, BottomSheetModalProvider} from '@gorhom/bottom-sheet';
import MindsDigitalModule, {MindsSDKResponse} from './MindsDigitalModule';
import {GestureHandlerRootView} from 'react-native-gesture-handler';

const App = () => {
  const [mindsSDKResponse, setMindsSDKResponse] = useState('');
  const [cpf, setCpf] = useState('');
  const [phone, setPhone] = useState('');
  const [status, setStatus] = useState('null');
  const [confidence, setConfidence] = useState('null');
  const [match, setMatch] = useState('null');
  const [disableEnrollment, setDisableEnrollment] = useState(false);
  const [disableVerification, setDisableVerification] = useState(false);

  const bottomSheetModalRef = useRef<BottomSheetModal>(null);
  const snapPoints = useMemo(() => ['90%', '90%'], []);
  const handlePresentModalPress = useCallback(() => {
    bottomSheetModalRef.current?.present();
  }, []);
  const handleSheetChanges = useCallback((_: number) => {}, []);

  return (
    <GestureHandlerRootView style={{flex: 1}}>
      <BottomSheetModalProvider>
        <View style={styles.container}>
          <KeyboardAwareScrollView>
            <View style={styles.introduction}>
              <Image style={styles.banner} source={require('./Banner.png')} />
              <Text style={styles.helloText}>Olá :) Seja Bem-vindo</Text>
              <Text>Preencha os dados abaixo e confira nosso conteúdo</Text>
            </View>

            <View style={styles.inputs}>
              <View style={styles.inputContainer}>
                <Icon name="contacts" size={20} color="#000" />
                <TextInput
                  style={styles.input}
                  keyboardType="numeric"
                  placeholder="CPF"
                  onChangeText={_cpf => setCpf(_cpf)}
                />
              </View>

              <View style={styles.inputContainer}>
                <Icon name="phone" size={20} color="#000" />
                <TextInput
                  style={styles.input}
                  keyboardType="numeric"
                  placeholder="Telefone + DDD"
                  onChangeText={_phone => setPhone(_phone)}
                />
              </View>
            </View>

            <View style={styles.buttons}>
              <View style={styles.baseButton}>
                <Button
                  disabled={disableEnrollment || disableVerification}
                  title="Cadastro por voz"
                  color="#17CEAB"
                  onPress={() => {
                    setDisableEnrollment(true);
                    MindsDigitalModule.enrollment(
                      cpf,
                      phone,
                      (response: MindsSDKResponse) => {
                        setMindsSDKResponse(JSON.stringify(response, null, 4));
                        handlePresentModalPress();
                        setDisableEnrollment(false);
                        setStatus(response.status ?? 'null');
                        setConfidence(response.confidence ?? 'null');
                        setMatch(response.matchPrediction ?? 'null');
                      },
                    );
                  }}
                />
              </View>

              <View style={styles.baseButton}>
                <Button
                  disabled={disableVerification || disableEnrollment}
                  title="Autenticação por voz"
                  color="#141540"
                  onPress={() => {
                    setDisableVerification(true);
                    MindsDigitalModule.enrollment(
                      cpf,
                      phone,
                      (response: MindsSDKResponse) => {
                        setMindsSDKResponse(JSON.stringify(response, null, 4));
                        handlePresentModalPress();
                        setDisableVerification(false);
                        setStatus(response.status ?? 'null');
                        setConfidence(response.confidence ?? 'null');
                        setMatch(response.matchPrediction ?? 'null');
                      },
                    );
                  }}
                />
              </View>
            </View>

            <View style={styles.logoContainer}>
              <Image style={styles.logo} source={require('./Logo.png')} />
            </View>
          </KeyboardAwareScrollView>
          <BottomSheetModal
            enablePanDownToClose={true}
            ref={bottomSheetModalRef}
            index={1}
            snapPoints={snapPoints}
            onChange={handleSheetChanges}>
            <View>
              <View style={styles.chip}>
                <Text>status: "{status}"</Text>
              </View>
              <View style={styles.chip}>
                <Text>confidence: "{confidence}"</Text>
              </View>
              <View style={styles.chip}>
                <Text>match: "{match}"</Text>
              </View>
            </View>
            <Text style={styles.responseTitle}>Lorem ipsum dolor</Text>
            <Text style={styles.responseDescription}>
              Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
              eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut Ut
              Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris
              nisi ut aliquip.
            </Text>
            <View style={styles.mindsSDKBody}>
              <Text style={{color: '#17CEAB'}}>{mindsSDKResponse}</Text>
            </View>
          </BottomSheetModal>
        </View>
      </BottomSheetModalProvider>
    </GestureHandlerRootView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  helloText: {
    fontSize: 24,
    color: '#242424',
  },
  banner: {
    width: 200,
    resizeMode: 'contain',
  },
  logo: {
    flex: 1,
    width: 100,
    resizeMode: 'contain',
    margin: 'auto',
  },
  baseButton: {
    marginBottom: 16,
  },
  input: {
    borderBottomWidth: 1,
    borderBottomColor: '#252525',
    padding: 10,
    flex: 1,
  },
  logoContainer: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  inputContainer: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  introduction: {
    flex: 1,
    textAlign: 'center',
    alignItems: 'center',
  },
  inputs: {
    flex: 1,
    marginTop: 16,
    marginBottom: 16,
  },
  buttons: {
    flex: 1,
    marginTop: 16,
    marginBottom: 16,
  },
  responseTitle: {
    fontFamily: 'Inter',
    fontStyle: 'normal',
    fontWeight: '700',
    fontSize: 18,
    lineHeight: 22,
    color: '#242424',
    margin: 16,
  },
  responseDescription: {
    fontFamily: 'Inter',
    fontStyle: 'normal',
    fontWeight: '400',
    fontSize: 16,
    lineHeight: 22,
    color: '#242424',
    margin: 16,
  },

  mindsSDKBody: {
    color: '#17CEAB',
    backgroundColor: '#E9E9E9',
    borderRadius: 30,
    padding: 30,
    margin: 16,
  },
  chip: {
    alignSelf: 'flex-start',
    marginLeft: 16,
    marginBottom: 8,
    borderRadius: 30,
    backgroundColor: '#D1F5EE',
    fontSize: 8,
    padding: 8,
  },
});

export default App;
