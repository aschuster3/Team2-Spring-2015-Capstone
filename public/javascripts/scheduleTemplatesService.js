angular.module('mwl.calendar')
  .factory('scheduleTemplatesService', ['$http', '$log', function ($http, $log) {

    var baseUrl = '/templates-api';

    var service = {
      getAll: getAllScheduleTemplates
    };

    return service;


    function getAllScheduleTemplates() {
      return $http.get(baseUrl).then(
        function success(response) {
          return response.data;
        },
        function failure(response) {
          $log.error(response);
          throw 'Error in retrieving templates';
        }
      );
    }
  }]);