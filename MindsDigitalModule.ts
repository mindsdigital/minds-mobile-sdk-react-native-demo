import { NativeModules } from 'react-native';

const { MindsDigitalModule } = NativeModules;



interface MindsDigitalInterface {
  enrollment(
    cpf: string,
    phone: string,
    callback: (response: string) => void,
  ): void;
  verification(
    cpf: string,
    phone: string,
    callback: (response: string) => void,
  ): void;
}

export default MindsDigitalModule as MindsDigitalInterface;
