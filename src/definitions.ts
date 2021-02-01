declare module '@capacitor/core' {
  interface PluginRegistry {
    MatiCapacitorPlugin: MatiCapacitorPluginPlugin;
  }
}

export interface MatiCapacitorPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
