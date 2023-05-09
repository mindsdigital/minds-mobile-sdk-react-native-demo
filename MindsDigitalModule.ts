import { NativeModules } from 'react-native';

const { MindsDigitalModule } = NativeModules;



interface MindsDigitalInterface {
  enrollment(
    cpf: string,
    phone: string,
  ): Promise<string>;
  authentication(
    cpf: string,
    phone: string,
  ): Promise<string>;
}

export default MindsDigitalModule as MindsDigitalInterface;
