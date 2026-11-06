import http from 'k6/http'; import {check} from 'k6';
export const options={vus:10,duration:'30s',thresholds:{http_req_failed:['rate<0.01'],http_req_duration:['p(95)<500']}};
const B='http://localhost:8080';
export function setup(){const r=http.post(B+'/api/v1/auth/login',JSON.stringify({username:__ENV.U,password:__ENV.P}),{headers:{'Content-Type':'application/json'}});return {t:r.json('data.token')};}
export default function(d){const h={headers:{Authorization:'Bearer '+d.t}};
 const a=http.get(B+'/api/v1/clients?page=0&size=20',h);check(a,{'clients 200':r=>r.status===200});
 const b=http.get(B+'/api/v1/drivers?page=0&size=20',h);check(b,{'drivers 200':r=>r.status===200});}
