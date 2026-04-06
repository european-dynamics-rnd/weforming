FROM node:22-alpine

RUN mkdir -p /w

WORKDIR /w

# Copy dependency list files
COPY package*.json /w

# Install dependencies
RUN npm install --production

# Copy source files
COPY . /w

# Application entry point
ENTRYPOINT ["node", "index.js"]
