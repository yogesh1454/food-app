module.exports = function (api) {
  api.cache(true);

  const env = process.env.APP_ENV || 'development';
  const envFile = `.env.${env}`;

  return {
    presets: ['babel-preset-expo'],
    plugins: [
      ['dotenv-import', {
        moduleName: '@env',
        path: envFile,
        safe: false,
        allowUndefined: true,
      }],
      'react-native-reanimated/plugin',
    ],
  };
};