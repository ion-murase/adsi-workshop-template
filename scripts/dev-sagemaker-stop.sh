#!/bin/bash
echo "Stopping services..."
fuser -k 3000/tcp 2>/dev/null || true
fuser -k 3001/tcp 2>/dev/null || true
fuser -k 8080/tcp 2>/dev/null || true
echo "All services stopped."
