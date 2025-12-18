#!/bin/bash
# Docker Build and Run Script for Rajida Report Service

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="rajida-report-service"
IMAGE_TAG="1.0.0"
CONTAINER_NAME="rajida-report"
PORT="8080"

echo -e "${YELLOW}================================${NC}"
echo -e "${YELLOW}Rajida Report Service - Docker${NC}"
echo -e "${YELLOW}================================${NC}"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker is installed${NC}"
echo ""

# Parse command line arguments
case "${1:-help}" in
    build)
        echo -e "${YELLOW}Building Docker image...${NC}"
        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
        echo -e "${GREEN}✓ Image built successfully: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
        ;;
    
    run)
        echo -e "${YELLOW}Running Docker container...${NC}"
        
        # Check if container already exists
        if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
            echo -e "${YELLOW}Removing existing container...${NC}"
            docker rm -f ${CONTAINER_NAME}
        fi
        
        docker run -d \
            --name ${CONTAINER_NAME} \
            -p ${PORT}:8080 \
            -v $(pwd)/uploads:/app/uploads \
            -e SPRING_PROFILES_ACTIVE=prod \
            ${IMAGE_NAME}:${IMAGE_TAG}
        
        echo -e "${GREEN}✓ Container started: ${CONTAINER_NAME}${NC}"
        echo -e "${GREEN}✓ Service available at: http://localhost:${PORT}${NC}"
        echo -e "${GREEN}✓ Uploads directory: $(pwd)/uploads${NC}"
        ;;
    
    logs)
        echo -e "${YELLOW}Showing container logs (Press Ctrl+C to exit)...${NC}"
        docker logs -f ${CONTAINER_NAME}
        ;;
    
    stop)
        echo -e "${YELLOW}Stopping container...${NC}"
        docker stop ${CONTAINER_NAME}
        echo -e "${GREEN}✓ Container stopped${NC}"
        ;;
    
    restart)
        echo -e "${YELLOW}Restarting container...${NC}"
        docker restart ${CONTAINER_NAME}
        echo -e "${GREEN}✓ Container restarted${NC}"
        ;;
    
    remove)
        echo -e "${YELLOW}Removing container...${NC}"
        docker stop ${CONTAINER_NAME} 2>/dev/null || true
        docker rm ${CONTAINER_NAME} 2>/dev/null || true
        echo -e "${GREEN}✓ Container removed${NC}"
        ;;
    
    clean)
        echo -e "${YELLOW}Removing image...${NC}"
        docker rmi ${IMAGE_NAME}:${IMAGE_TAG}
        echo -e "${GREEN}✓ Image removed${NC}"
        ;;
    
    status)
        echo -e "${YELLOW}Container Status:${NC}"
        docker ps -f name=${CONTAINER_NAME} --no-trunc
        ;;
    
    health)
        echo -e "${YELLOW}Checking service health...${NC}"
        if curl -s http://localhost:${PORT}/api/reports/health > /dev/null; then
            echo -e "${GREEN}✓ Service is healthy${NC}"
            curl -s http://localhost:${PORT}/api/reports/health | jq '.'
        else
            echo -e "${RED}✗ Service is not responding${NC}"
        fi
        ;;
    
    full-setup)
        echo -e "${YELLOW}Running full setup (build + run)...${NC}"
        $0 build
        echo ""
        $0 run
        echo ""
        sleep 2
        $0 health
        ;;
    
    help|*)
        echo "Usage: $0 {command}"
        echo ""
        echo "Commands:"
        echo "  build       - Build Docker image"
        echo "  run         - Run Docker container"
        echo "  logs        - Show container logs"
        echo "  stop        - Stop running container"
        echo "  restart     - Restart container"
        echo "  remove      - Remove container"
        echo "  clean       - Remove image"
        echo "  status      - Show container status"
        echo "  health      - Check service health"
        echo "  full-setup  - Build and run"
        echo ""
        echo "Example:"
        echo "  $0 full-setup   # Build and start the service"
        echo "  $0 logs         # View application logs"
        echo "  $0 health       # Check if service is running"
        ;;
esac
