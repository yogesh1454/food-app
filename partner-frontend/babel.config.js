<<<<<<< HEAD
module.exports = function (api) {
=======
module.exports = function(api) {
>>>>>>> origin/partner-frontend
  api.cache(true);
  return {
    presets: ['babel-preset-expo'],
    plugins: [
      'react-native-reanimated/plugin',
<<<<<<< HEAD
      ['dotenv-import', {
        moduleName: '@env',
        path: '.env',
        blacklist: null,
        whitelist: null,
        safe: false,
        allowUndefined: true,
      }],
=======
>>>>>>> origin/partner-frontend
    ],
  };
};