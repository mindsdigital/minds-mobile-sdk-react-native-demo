import React, { useCallback, useMemo, useRef, useState } from 'react';
import { Alert, Button, Image, StyleSheet, Text, TextInput, View } from 'react-native';

import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { BottomSheetModal, BottomSheetModalProvider, BottomSheetScrollView } from '@gorhom/bottom-sheet';
import MindsDigitalModule, { } from './MindsDigitalModule';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { TextInputMask } from "react-native-masked-text";

const App = () => {
  const [mindsSDKResponse, setMindsSDKResponse] = useState('');
  const [cpf, setCpf] = useState('');
  const [phone, setPhone] = useState('');
  const [disableEnrollment, setDisableEnrollment] = useState(false);
  const [disableVerification, setDisableVerification] = useState(false);

  const bottomSheetModalRef = useRef<BottomSheetModal>(null);
  const snapPoints = useMemo(() => ['90%', '90%'], []);
  const handlePresentModalPress = useCallback(() => {
    bottomSheetModalRef.current?.present();
  }, []);
  const handleSheetChanges = useCallback((_: number) => { }, []);

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
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
                <TextInputMask
                  style={styles.input}
                  type={'cpf'}
                  placeholder="CPF"
                  value={cpf}
                  onChangeText={_cpf => {
                    setCpf(_cpf);
                  }}
                />
              </View>
              {!(cpf.length > 0) && (
                <Text style={{ color: 'red' }}>obrigatório</Text>
              )}

              <View style={styles.inputContainer}>
                <Icon name="phone" size={20} color="#000" />
                <TextInputMask
                  style={styles.input}
                  type={'cel-phone'}
                  options={{
                    maskType: 'BRL',
                    withDDD: true,
                    dddMask: '(99) ',
                  }}
                  placeholder="Telefone + DDD"
                  value={phone}
                  onChangeText={_phone => {
                    setPhone(_phone);
                  }}
                />
              </View>
              {!(phone.length > 0) && (
                <Text style={{ color: 'red' }}>obrigatório</Text>
              )}
            </View>

            <View style={styles.buttons}>
              <View style={styles.baseButton}>
                <Button
                  disabled={disableEnrollment || disableVerification}
                  title="Cadastro por voz"
                  color="#17CEAB"
                  onPress={() => {
                    if (!cpf || !phone) {
                      Alert.alert("Atenção", "CPF e telefone são obrigatórios");
                      return;
                    }
                    setDisableEnrollment(true);
                    MindsDigitalModule.enrollment(
                      cpf,
                      phone,
                      (response: string) => {
                        try {
                          let jsonString = JSON.parse(response)
                          setMindsSDKResponse(JSON.stringify(jsonString, null, 4));
                        } catch (error) {
                          console.log(response)
                          setMindsSDKResponse(JSON.stringify(response, null, 4));
                        }
                        handlePresentModalPress();
                        setDisableEnrollment(false);
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
                    if (!cpf || !phone) {
                      Alert.alert("Atenção", "CPF e telefone são obrigatórios");
                      return;
                    }
                    setDisableVerification(true);
                    MindsDigitalModule.verification(
                      cpf,
                      phone,
                      (response: string) => {
                        try {
                          let jsonString = JSON.parse(response)
                          setMindsSDKResponse(JSON.stringify(jsonString, null, 4));
                        } catch (error) {
                          console.log(response)
                          setMindsSDKResponse(JSON.stringify(response, null, 4));
                        }
                        handlePresentModalPress();
                        setDisableVerification(false);
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
            <BottomSheetScrollView>
              <Text style={styles.responseTitle}>Resultado da operação</Text>
              <View style={styles.mindsSDKBody}>
                <Text style={{ color: '#17CEAB' }}>{mindsSDKResponse}</Text>
              </View>
            </BottomSheetScrollView>
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
    fontStyle: 'normal',
    fontWeight: '700',
    fontSize: 18,
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

});

export default App;
