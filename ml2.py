# import tensorflow as tf
# from tensorflow import keras
# from tensorflow.keras import layers

# print("TensorFlow version:", tf.__version__)
# print("Num GPUs Available: ", len(tf.config.list_physical_devices('GPU')))

# # Define Sequential model with 3 layers
# model = keras.Sequential(
#     [
#         layers.Dense(2, activation="relu", name="layer1"),
#         layers.Dense(3, activation="relu", name="layer2"),
#         layers.Dense(4, name="layer3"),
#     ]
# )
# # Call model on a test input
# x = tf.ones((3, 3))
# y = model(x)

# mnist = tf.keras.datasets.mnist

# (x_train, y_train), (x_test, y_test) = mnist.load_data()
# x_train, x_test = x_train / 255.0, x_test / 255.0

# model = tf.keras.models.Sequential([
#   tf.keras.layers.Flatten(input_shape=(28, 28)),
#   tf.keras.layers.Dense(128, activation='relu'),
#   tf.keras.layers.Dropout(0.2),
#   tf.keras.layers.Dense(10)
# ])

# predictions = model(x_train[:2]).numpy()
# print(tf.nn.softmax(predictions).numpy())
# loss_fn = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True)
# print(loss_fn(y_train[:2], predictions).numpy())
# model.compile(optimizer='adam',
#               loss=loss_fn,
#               metrics=['accuracy'])
# # print(x_train)
# # print(y_train)
# model.fit(x_train, y_train, epochs=5)

# print(model.evaluate(x_test,  y_test, verbose=2))

import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras.layers import Input, Dense, Flatten, Conv2D
from tensorflow.keras import Model
from tensorflow.keras.optimizers import Adam


def loss(y_true, y_pred):
    """Loss function"""
    return tf.square(y_true - y_pred)


if __name__ == '__main__':

    # Learn to sum 20 nums, train and test datasets
    train_samples = tf.random.normal(shape=(10000, 20))
    train_targets = tf.reduce_sum(train_samples, axis=-1)
    test_samples = tf.random.normal(shape=(100, 20))
    test_targets = tf.reduce_sum(test_samples, axis=-1)

    # Model building with Keras functional API
    x = Input(shape=[20])
    h = Dense(units=20, activation='relu')(x)
    h = Dense(units=10, activation='relu')(h)
    y = Dense(units=1)(h)
    model = Model(x,y)

    # Compiling model
    model.compile(
        optimizer=Adam(learning_rate=0.001),
        loss=loss,
        metrics=['mse'])

    # Training
    model.fit(
        x=train_samples, y=train_targets,
        batch_size=1, 
        epochs=10,
        validation_data=(test_samples, test_targets),
        shuffle=True)