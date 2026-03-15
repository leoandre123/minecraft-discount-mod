package com.leodenandre.discount.client;

public interface ClientCameraBridge {
    void openCameraUi();
    void closeCameraUi();
    void toggleCameraUi();
    void takePicture();

    ClientCameraBridge DUMMY = new ClientCameraBridge() {

        @Override
        public void openCameraUi() {

        }

        @Override
        public void closeCameraUi() {

        }

        @Override
        public void toggleCameraUi() {

        }

        @Override
        public void takePicture() {

        }
    };
}

