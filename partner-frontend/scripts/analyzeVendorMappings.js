/**
 * Script to list all userMappings and find matching vendors
 */

const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');

const PROJECT_ID = 'food-app-1feee';
const COLLECTION_ID = 'userMappings';
const BACKEND_URL = 'http://54.87.117.181:8080';

function getAccessToken() {
    const configPath = path.join(process.env.HOME, '.config/configstore/firebase-tools.json');
    try {
        const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
        return config.tokens?.access_token;
    } catch (e) {
        console.error('Could not read Firebase config:', e.message);
        return null;
    }
}

function httpsGet(url, headers = {}) {
    return new Promise((resolve, reject) => {
        const urlObj = new URL(url);
        const client = urlObj.protocol === 'https:' ? https : http;

        const options = {
            hostname: urlObj.hostname,
            port: urlObj.port || (urlObj.protocol === 'https:' ? 443 : 80),
            path: urlObj.pathname + urlObj.search,
            method: 'GET',
            headers
        };

        const req = client.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, data: JSON.parse(body) });
                } catch (e) {
                    resolve({ status: res.statusCode, data: body });
                }
            });
        });

        req.on('error', reject);
        req.end();
    });
}

async function listUserMappings(accessToken) {
    const url = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/${COLLECTION_ID}`;
    const result = await httpsGet(url, { 'Authorization': `Bearer ${accessToken}` });

    if (result.status !== 200) {
        console.error('Failed to fetch userMappings:', result.data);
        return [];
    }

    const documents = result.data.documents || [];
    return documents.map(doc => {
        const firebaseUid = doc.name.split('/').pop();
        const fields = doc.fields || {};
        return {
            firebaseUid,
            uuid: fields.uuid?.stringValue || null,
            vendorId: fields.vendorId?.integerValue || null,
            createdAt: fields.createdAt?.stringValue || null
        };
    });
}

async function getAllVendors() {
    const vendors = [];
    for (let id = 1; id <= 30; id++) {
        try {
            const result = await httpsGet(`${BACKEND_URL}/api/v1/vendors/${id}`);
            if (result.status === 200 && result.data.vendorId) {
                vendors.push(result.data);
            }
        } catch (e) {
            // Vendor doesn't exist
        }
    }
    return vendors;
}

async function main() {
    console.log('Analyzing userMappings and vendors...\n');

    const accessToken = getAccessToken();
    if (!accessToken) {
        console.error('No access token found. Please run "firebase login" first.');
        process.exit(1);
    }

    // Get all userMappings from Firestore
    console.log('Fetching userMappings from Firestore...');
    const userMappings = await listUserMappings(accessToken);
    console.log(`Found ${userMappings.length} userMappings\n`);

    // Get all vendors from backend
    console.log('Fetching vendors from backend...');
    const vendors = await getAllVendors();
    console.log(`Found ${vendors.length} vendors\n`);

    console.log('='.repeat(80));
    console.log('USER MAPPINGS IN FIRESTORE:');
    console.log('='.repeat(80));

    for (const mapping of userMappings) {
        console.log(`\nFirebase UID: ${mapping.firebaseUid}`);
        console.log(`  UUID: ${mapping.uuid || 'N/A'}`);
        console.log(`  VendorId: ${mapping.vendorId || 'NOT SET ❌'}`);
        console.log(`  Created: ${mapping.createdAt || 'N/A'}`);
    }

    console.log('\n' + '='.repeat(80));
    console.log('VENDORS IN BACKEND:');
    console.log('='.repeat(80));

    for (const vendor of vendors) {
        console.log(`\nVendor ${vendor.vendorId}: ${vendor.companyName}`);
        console.log(`  Email: ${vendor.companyEmail}`);
        console.log(`  Phone: ${vendor.companyPhone}`);
        console.log(`  Branches: ${vendor.branches?.length || 0}`);
    }

    console.log('\n' + '='.repeat(80));
    console.log('USERS WITHOUT VENDORID (need to be updated):');
    console.log('='.repeat(80));

    const needsUpdate = userMappings.filter(m => !m.vendorId);
    if (needsUpdate.length === 0) {
        console.log('\n✅ All users already have vendorId set!');
    } else {
        for (const mapping of needsUpdate) {
            console.log(`\n❌ Firebase UID: ${mapping.firebaseUid}`);
            console.log(`   UUID: ${mapping.uuid}`);
            console.log('   -> Needs manual matching with a vendor');
        }
    }

    console.log('\n');
}

main();
