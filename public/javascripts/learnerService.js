angular.module('mwl.calendar')
    .factory('learnerService', ['$http', function($http) {
        var urlBase = '/learners-api';
        var service = {};

        /* On success, data will contain...
             for coordinators: their assigned learners
             for admins: all learners
         */
        service.getLearners = function () {
            return $http.get(urlBase);
        }

        return service;
    }]);