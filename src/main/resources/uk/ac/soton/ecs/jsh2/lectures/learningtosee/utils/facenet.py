import torch
import numpy as np
from facenet_pytorch import MTCNN, InceptionResnetV1, extract_face
from PIL import Image

device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

mtcnn = MTCNN(
    image_size=160, margin=0, min_face_size=20,
    thresholds=[0.6, 0.7, 0.7], factor=0.709, prewhiten=True,
    keep_all=True, device=device
)

resnet = InceptionResnetV1(pretrained='vggface2').eval().to(device)


def createImage(width, height, bytedata):
	return Image.frombytes('RGB', (width, height), bytedata, 'raw')


def detectEmbed(width, height, bytedata):
	img = createImage(width, height, bytedata)
	boxes, probs = mtcnn.detect(img)
	if boxes is None:
		return None, None, None
	embeddings = []
	for i, box in enumerate(boxes):
	    img_cropped = extract_face(img, box)
	    img_embedding = resnet(img_cropped.unsqueeze(0))
	    embeddings.append(img_embedding.cpu().detach().numpy())
	embeddings = np.array(embeddings)
	return boxes, probs, embeddings

def detect(width, height, bytedata):
	img = createImage(width, height, bytedata)
	boxes, probs = mtcnn.detect(img)
	return boxes, probs
