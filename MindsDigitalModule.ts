import { NativeModules } from 'react-native';

const { MindsDigitalModule } = NativeModules;



interface MindsDigitalInterface {
  enrollment(
    document: string,
    phone: string,
  ): Promise<string>;
  authentication(
    document: string,
    phone: string,
  ): Promise<string>;
}

export default MindsDigitalModule as MindsDigitalInterface;
