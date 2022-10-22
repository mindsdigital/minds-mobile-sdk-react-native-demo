import {NativeModules} from 'react-native';

const {MindsDigitalModule} = NativeModules;

export interface MindsSDKResponse {
  id?: number;
  success?: boolean;
  message?: string;
  externalId?: string;
  status?: string;
  cpf?: string;
  verificationId?: string;
  action?: string;
  whitelisted?: boolean;
  fraudRisk?: string;
  enrollmentExternalId?: string;
  matchPrediction?: string;
  confidence?: string;
}

interface MindsDigitalInterface {
  enrollment(
    cpf: string,
    phone: string,
    callback: (response: MindsSDKResponse) => void,
  ): void;
  verification(
    cpf: string,
    phone: string,
    callback: (response: MindsSDKResponse) => void,
  ): void;
}

export default MindsDigitalModule as MindsDigitalInterface;
