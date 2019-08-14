import keras
from keras_retinanet import models
from keras_retinanet.utils.image import preprocess_image, resize_image
from PIL import Image
import numpy as np


model = models.load_model("/Users/jsh2/ownCloud/Outreach/Smallpeice-Lecture/resnet50_coco_best_v2.1.0.h5", backbone_name='resnet50')


def createImage(width, height, bytedata):
	rgb = np.asarray(Image.frombytes('RGB', (width, height), bytedata, 'raw'))
	return rgb[:, :, ::-1]


def detect(width, height, bytedata):
	image = createImage(width, height, bytedata)
	image = preprocess_image(image)
	# image, scale = resize_image(image)
	boxes, scores, labels = model.predict_on_batch(np.expand_dims(image, axis=0))
	# boxes /= scale
	return boxes[0], scores[0].tolist(), labels[0].tolist()

