angular.module('mwl.calendar')
  .factory('notificationModalService', ['$modal', function ($modal) {
    return {
      show: showNotification
    };

    function showNotification(notificationMessage, modalTitle) {
      $modal.open({
        templateUrl: 'assets/templates/notificationModalTemplate.html',
        controller: function ($scope, $modalInstance) {
          $scope.message = notificationMessage;
          $scope.title = modalTitle;
          $scope.close = function () {
            $modalInstance.close();
          };
        },
        size: 'sm'
      });
    }
  }]);