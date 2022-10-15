import React, {useState} from 'react';
import {
  Button,
  Image,
  NativeModules,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scroll-view';
import Icon from 'react-native-vector-icons/MaterialIcons';
import {BottomSheetModalProvider} from '@gorhom/bottom-sheet';

const App = () => {
  const [modalVisible, setModalVisible] = useState(false);

  return (
    <BottomSheetModalProvider>
      <KeyboardAwareScrollView>
        <View style={styles.container}>
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
              />
            </View>

            <View style={styles.inputContainer}>
              <Icon name="phone" size={20} color="#000" />
              <TextInput
                style={styles.input}
                keyboardType="numeric"
                placeholder="Telefone + DDD"
              />
            </View>
          </View>

          <View style={styles.buttons}>
            <View style={styles.baseButton}>
              <Button
                title="Cadastro por voz"
                color="#17CEAB"
                onPress={() => NativeModules}
              />
            </View>

            <View style={styles.baseButton}>
              <Button
                title="Autenticação por voz"
                color="#141540"
                onPress={() => setModalVisible(true)}
              />
            </View>
          </View>

          <View style={styles.logoContainer}>
            <Image style={styles.logo} source={require('./Logo.png')} />
          </View>
        </View>
      </KeyboardAwareScrollView>
    </BottomSheetModalProvider>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'space-between',
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
});

export default App;
