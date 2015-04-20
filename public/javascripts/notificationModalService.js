angular.module('mwl.calendar')
  .factory('notificationModalService', ['$modal', function ($modal) {
    return {
      show: showNotification
    };

    /**
     * notificationMessages is either a single string,
     * or an array of strings, where each string represents
     * a single line in the modal.
     */
    function showNotification(notificationMessages, modalTitle) {
      if (typeof notificationMessages === 'string') {
        notificationMessages = [notificationMessages];
      }

      $modal.open({
        templateUrl: 'assets/templates/notificationModalTemplate.html',
        controller: function ($scope, $modalInstance) {
          $scope.messages = notificationMessages;
          $scope.title = modalTitle;
          $scope.close = function () {
            $modalInstance.close();
          };
        },
        size: 'sm'
      });
    }
  }]);