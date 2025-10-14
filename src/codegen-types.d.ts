declare module 'react-native/Libraries/Types/CodegenTypes' {
  export type DirectEventHandler<T = {}> = (event: { nativeEvent: T }) => void;
  export type BubblingEventHandler<T = {}> = (event: {
    nativeEvent: T;
  }) => void;
}

declare module 'react-native/Libraries/Utilities/codegenNativeComponent' {
  import type { HostComponent } from 'react-native';
  export default function codegenNativeComponent<T>(
    componentName: string,
    options?: {}
  ): HostComponent<T>;
}
