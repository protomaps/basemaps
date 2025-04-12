(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const r of document.querySelectorAll('link[rel="modulepreload"]'))s(r);new MutationObserver(r=>{for(const i of r)if(i.type==="childList")for(const o of i.addedNodes)o.tagName==="LINK"&&o.rel==="modulepreload"&&s(o)}).observe(document,{childList:!0,subtree:!0});function n(r){const i={};return r.integrity&&(i.integrity=r.integrity),r.referrerPolicy&&(i.referrerPolicy=r.referrerPolicy),r.crossOrigin==="use-credentials"?i.credentials="include":r.crossOrigin==="anonymous"?i.credentials="omit":i.credentials="same-origin",i}function s(r){if(r.ep)return;r.ep=!0;const i=n(r);fetch(r.href,i)}})();const ae=!1,de=(e,t)=>e===t,he=Symbol("solid-track"),D={equals:de};let se=oe;const N=1,F=2,re={owned:null,cleanups:null,context:null,owner:null},X={};var g=null;let Y=null,pe=null,d=null,y=null,T=null,G=0;function U(e,t){const n=d,s=g,r=e.length===0,i=t===void 0?s:t,o=r?re:{owned:null,cleanups:null,context:i?i.context:null,owner:i},l=r?e:()=>e(()=>E(()=>k(o)));g=o,d=null;try{return $(l,!0)}finally{d=n,g=s}}function _(e,t){t=t?Object.assign({},D,t):D;const n={value:e,observers:null,observerSlots:null,comparator:t.equals||void 0},s=r=>(typeof r=="function"&&(r=r(n.value)),le(n,r));return[ie.bind(n),s]}function ge(e,t,n){const s=K(e,t,!0,N);j(s)}function H(e,t,n){const s=K(e,t,!1,N);j(s)}function we(e,t,n){se=ve;const s=K(e,t,!1,N);s.user=!0,T?T.push(s):j(s)}function B(e,t,n){n=n?Object.assign({},D,n):D;const s=K(e,t,!0,0);return s.observers=null,s.observerSlots=null,s.comparator=n.equals||void 0,j(s),ie.bind(s)}function ye(e){return e&&typeof e=="object"&&"then"in e}function Ie(e,t,n){let s,r,i;s=!0,r=e,i={};let o=null,l=X,u=!1,c="initialValue"in i,a=typeof s=="function"&&B(s);const f=new Set,[v,m]=(i.storage||_)(i.initialValue),[x,C]=_(void 0),[M,L]=_(void 0,{equals:!1}),[S,A]=_(c?"ready":"unresolved");function w(b,h,p,P){return o===b&&(o=null,P!==void 0&&(c=!0),(b===l||h===l)&&i.onHydrated&&queueMicrotask(()=>i.onHydrated(P,{value:h})),l=X,O(h,p)),h}function O(b,h){$(()=>{h===void 0&&m(()=>b),A(h!==void 0?"errored":c?"ready":"unresolved"),C(h);for(const p of f.keys())p.decrement();f.clear()},!1)}function V(){const b=me,h=v(),p=x();if(p!==void 0&&!o)throw p;return d&&d.user,h}function Q(b=!0){if(b!==!1&&u)return;u=!1;const h=a?a():s;if(h==null||h===!1){w(o,E(v));return}const p=l!==X?l:E(()=>r(h,{value:v(),refetching:b}));return ye(p)?(o=p,"value"in p?(p.status==="success"?w(o,p.value,void 0,h):w(o,void 0,Z(p.value),h),p):(u=!0,queueMicrotask(()=>u=!1),$(()=>{A(c?"refreshing":"pending"),L()},!1),p.then(P=>w(p,P,void 0,h),P=>w(p,void 0,Z(P),h)))):(w(o,p,void 0,h),p)}return Object.defineProperties(V,{state:{get:()=>S()},error:{get:()=>x()},loading:{get(){const b=S();return b==="pending"||b==="refreshing"}},latest:{get(){if(!c)return V();const b=x();if(b&&!o)throw b;return v()}}}),a?ge(()=>Q(!1)):Q(!1),[V,{refetch:Q,mutate:m}]}function E(e){if(d===null)return e();const t=d;d=null;try{return e()}finally{d=t}}function _e(e){we(()=>E(e))}function be(e){return g===null||(g.cleanups===null?g.cleanups=[e]:g.cleanups.push(e)),e}const[je,Me]=_(!1);let me;function ie(){if(this.sources&&this.state)if(this.state===N)j(this);else{const e=y;y=null,$(()=>q(this),!1),y=e}if(d){const e=this.observers?this.observers.length:0;d.sources?(d.sources.push(this),d.sourceSlots.push(e)):(d.sources=[this],d.sourceSlots=[e]),this.observers?(this.observers.push(d),this.observerSlots.push(d.sources.length-1)):(this.observers=[d],this.observerSlots=[d.sources.length-1])}return this.value}function le(e,t,n){let s=e.value;return(!e.comparator||!e.comparator(s,t))&&(e.value=t,e.observers&&e.observers.length&&$(()=>{for(let r=0;r<e.observers.length;r+=1){const i=e.observers[r],o=Y&&Y.running;o&&Y.disposed.has(i),(o?!i.tState:!i.state)&&(i.pure?y.push(i):T.push(i),i.observers&&fe(i)),o||(i.state=N)}if(y.length>1e6)throw y=[],new Error},!1)),t}function j(e){if(!e.fn)return;k(e);const t=G;Se(e,e.value,t)}function Se(e,t,n){let s;const r=g,i=d;d=g=e;try{s=e.fn(t)}catch(o){return e.pure&&(e.state=N,e.owned&&e.owned.forEach(k),e.owned=null),e.updatedAt=n+1,ue(o)}finally{d=i,g=r}(!e.updatedAt||e.updatedAt<=n)&&(e.updatedAt!=null&&"observers"in e?le(e,s):e.value=s,e.updatedAt=n)}function K(e,t,n,s=N,r){const i={fn:e,state:s,updatedAt:null,owned:null,sources:null,sourceSlots:null,cleanups:null,value:t,owner:g,context:g?g.context:null,pure:n};return g===null||g!==re&&(g.owned?g.owned.push(i):g.owned=[i]),i}function R(e){if(e.state===0)return;if(e.state===F)return q(e);if(e.suspense&&E(e.suspense.inFallback))return e.suspense.effects.push(e);const t=[e];for(;(e=e.owner)&&(!e.updatedAt||e.updatedAt<G);)e.state&&t.push(e);for(let n=t.length-1;n>=0;n--)if(e=t[n],e.state===N)j(e);else if(e.state===F){const s=y;y=null,$(()=>q(e,t[0]),!1),y=s}}function $(e,t){if(y)return e();let n=!1;t||(y=[]),T?n=!0:T=[],G++;try{const s=e();return Ae(n),s}catch(s){n||(T=null),y=null,ue(s)}}function Ae(e){if(y&&(oe(y),y=null),e)return;const t=T;T=null,t.length&&$(()=>se(t),!1)}function oe(e){for(let t=0;t<e.length;t++)R(e[t])}function ve(e){let t,n=0;for(t=0;t<e.length;t++){const s=e[t];s.user?e[n++]=s:R(s)}for(t=0;t<n;t++)R(e[t])}function q(e,t){e.state=0;for(let n=0;n<e.sources.length;n+=1){const s=e.sources[n];if(s.sources){const r=s.state;r===N?s!==t&&(!s.updatedAt||s.updatedAt<G)&&R(s):r===F&&q(s,t)}}}function fe(e){for(let t=0;t<e.observers.length;t+=1){const n=e.observers[t];n.state||(n.state=F,n.pure?y.push(n):T.push(n),n.observers&&fe(n))}}function k(e){let t;if(e.sources)for(;e.sources.length;){const n=e.sources.pop(),s=e.sourceSlots.pop(),r=n.observers;if(r&&r.length){const i=r.pop(),o=n.observerSlots.pop();s<r.length&&(i.sourceSlots[o]=s,r[s]=i,n.observerSlots[s]=o)}}if(e.tOwned){for(t=e.tOwned.length-1;t>=0;t--)k(e.tOwned[t]);delete e.tOwned}if(e.owned){for(t=e.owned.length-1;t>=0;t--)k(e.owned[t]);e.owned=null}if(e.cleanups){for(t=e.cleanups.length-1;t>=0;t--)e.cleanups[t]();e.cleanups=null}e.state=0}function Z(e){return e instanceof Error?e:new Error(typeof e=="string"?e:"Unknown error",{cause:e})}function ue(e,t=g){throw Z(e)}const xe=Symbol("fallback");function ee(e){for(let t=0;t<e.length;t++)e[t]()}function Ee(e,t,n={}){let s=[],r=[],i=[],o=0,l=t.length>1?[]:null;return be(()=>ee(i)),()=>{let u=e()||[],c=u.length,a,f;return u[he],E(()=>{let m,x,C,M,L,S,A,w,O;if(c===0)o!==0&&(ee(i),i=[],s=[],r=[],o=0,l&&(l=[])),n.fallback&&(s=[xe],r[0]=U(V=>(i[0]=V,n.fallback())),o=1);else if(o===0){for(r=new Array(c),f=0;f<c;f++)s[f]=u[f],r[f]=U(v);o=c}else{for(C=new Array(c),M=new Array(c),l&&(L=new Array(c)),S=0,A=Math.min(o,c);S<A&&s[S]===u[S];S++);for(A=o-1,w=c-1;A>=S&&w>=S&&s[A]===u[w];A--,w--)C[w]=r[A],M[w]=i[A],l&&(L[w]=l[A]);for(m=new Map,x=new Array(w+1),f=w;f>=S;f--)O=u[f],a=m.get(O),x[f]=a===void 0?-1:a,m.set(O,f);for(a=S;a<=A;a++)O=s[a],f=m.get(O),f!==void 0&&f!==-1?(C[f]=r[a],M[f]=i[a],l&&(L[f]=l[a]),f=x[f],m.set(O,f)):i[a]();for(f=S;f<c;f++)f in C?(r[f]=C[f],i[f]=M[f],l&&(l[f]=L[f],l[f](f))):r[f]=U(v);r=r.slice(0,o=c),s=u.slice(0)}return r});function v(m){if(i[f]=m,l){const[x,C]=_(f);return l[f]=C,t(u[f],x)}return t(u[f])}}}function Ve(e,t){return E(()=>e(t||{}))}const Ce=e=>`Stale read from <${e}>.`;function Be(e){const t="fallback"in e&&{fallback:()=>e.fallback};return B(Ee(()=>e.each,e.children,t||void 0))}function ke(e){const t=e.keyed,n=B(()=>e.when,void 0,void 0),s=t?n:B(n,void 0,{equals:(r,i)=>!r==!i});return B(()=>{const r=s();if(r){const i=e.children;return typeof i=="function"&&i.length>0?E(()=>i(t?r:()=>{if(!E(s))throw Ce("Show");return n()})):i}return e.fallback},void 0,void 0)}function Te(e,t,n){let s=n.length,r=t.length,i=s,o=0,l=0,u=t[r-1].nextSibling,c=null;for(;o<r||l<i;){if(t[o]===n[l]){o++,l++;continue}for(;t[r-1]===n[i-1];)r--,i--;if(r===o){const a=i<s?l?n[l-1].nextSibling:n[i-l]:u;for(;l<i;)e.insertBefore(n[l++],a)}else if(i===l)for(;o<r;)(!c||!c.has(t[o]))&&t[o].remove(),o++;else if(t[o]===n[i-1]&&n[l]===t[r-1]){const a=t[--r].nextSibling;e.insertBefore(n[l++],t[o++].nextSibling),e.insertBefore(n[--i],a),t[r]=n[i]}else{if(!c){c=new Map;let f=l;for(;f<i;)c.set(n[f],f++)}const a=c.get(t[o]);if(a!=null)if(l<a&&a<i){let f=o,v=1,m;for(;++f<r&&f<i&&!((m=c.get(t[f]))==null||m!==a+v);)v++;if(v>a-l){const x=t[o];for(;l<a;)e.insertBefore(n[l++],x)}else e.replaceChild(n[l++],t[o++])}else o++;else t[o++].remove()}}}const te="_$DX_DELEGATE";function Ue(e,t,n,s={}){let r;return U(i=>{r=i,t===document?e():ce(t,e(),t.firstChild?null:void 0,n)},s.owner),()=>{r(),t.textContent=""}}function Ne(e,t,n,s){let r;const i=()=>{const l=document.createElement("template");return l.innerHTML=e,l.content.firstChild},o=()=>(r||(r=i())).cloneNode(!0);return o.cloneNode=o,o}function De(e,t=window.document){const n=t[te]||(t[te]=new Set);for(let s=0,r=e.length;s<r;s++){const i=e[s];n.has(i)||(n.add(i),t.addEventListener(i,Oe))}}function Fe(e,t,n){n==null?e.removeAttribute(t):e.setAttribute(t,n)}function J(e,t){t==null?e.removeAttribute("class"):e.className=t}function He(e,t,n){return E(()=>e(t,n))}function ce(e,t,n,s){if(n!==void 0&&!s&&(s=[]),typeof t!="function")return W(e,t,s,n);H(r=>W(e,t(),r,n),s)}function Oe(e){let t=e.target;const n=`$$${e.type}`,s=e.target,r=e.currentTarget,i=u=>Object.defineProperty(e,"target",{configurable:!0,value:u}),o=()=>{const u=t[n];if(u&&!t.disabled){const c=t[`${n}Data`];if(c!==void 0?u.call(t,c,e):u.call(t,e),e.cancelBubble)return}return t.host&&typeof t.host!="string"&&!t.host._$host&&t.contains(e.target)&&i(t.host),!0},l=()=>{for(;o()&&(t=t._$host||t.parentNode||t.host););};if(Object.defineProperty(e,"currentTarget",{configurable:!0,get(){return t||document}}),e.composedPath){const u=e.composedPath();i(u[0]);for(let c=0;c<u.length-2&&(t=u[c],!!o());c++){if(t._$host){t=t._$host,l();break}if(t.parentNode===r)break}}else l();i(s)}function W(e,t,n,s,r){for(;typeof n=="function";)n=n();if(t===n)return n;const i=typeof t,o=s!==void 0;if(e=o&&n[0]&&n[0].parentNode||e,i==="string"||i==="number"){if(i==="number"&&(t=t.toString(),t===n))return n;if(o){let l=n[0];l&&l.nodeType===3?l.data!==t&&(l.data=t):l=document.createTextNode(t),n=I(e,n,s,l)}else n!==""&&typeof n=="string"?n=e.firstChild.data=t:n=e.textContent=t}else if(t==null||i==="boolean")n=I(e,n,s);else{if(i==="function")return H(()=>{let l=t();for(;typeof l=="function";)l=l();n=W(e,l,n,s)}),()=>n;if(Array.isArray(t)){const l=[],u=n&&Array.isArray(n);if(z(l,t,n,r))return H(()=>n=W(e,l,n,s,!0)),()=>n;if(l.length===0){if(n=I(e,n,s),o)return n}else u?n.length===0?ne(e,l,s):Te(e,n,l):(n&&I(e),ne(e,l));n=l}else if(t.nodeType){if(Array.isArray(n)){if(o)return n=I(e,n,s,t);I(e,n,null,t)}else n==null||n===""||!e.firstChild?e.appendChild(t):e.replaceChild(t,e.firstChild);n=t}}return n}function z(e,t,n,s){let r=!1;for(let i=0,o=t.length;i<o;i++){let l=t[i],u=n&&n[e.length],c;if(!(l==null||l===!0||l===!1))if((c=typeof l)=="object"&&l.nodeType)e.push(l);else if(Array.isArray(l))r=z(e,l,u)||r;else if(c==="function")if(s){for(;typeof l=="function";)l=l();r=z(e,Array.isArray(l)?l:[l],Array.isArray(u)?u:[u])||r}else e.push(l),r=!0;else{const a=String(l);u&&u.nodeType===3&&u.data===a?e.push(u):e.push(document.createTextNode(a))}}return r}function ne(e,t,n=null){for(let s=0,r=t.length;s<r;s++)e.insertBefore(t[s],n)}function I(e,t,n,s){if(n===void 0)return e.textContent="";const r=s||document.createTextNode("");if(t.length){let i=!1;for(let o=t.length-1;o>=0;o--){const l=t[o];if(r!==l){const u=l.parentNode===e;!i&&!o?u?e.replaceChild(r,l):e.insertBefore(r,n):u&&l.remove()}else i=!0}}else e.insertBefore(r,n);return[r]}var $e=Ne('<nav class="max-w-[1500px] mx-auto py-4 space-x-4"><a href=/>Basemap</a><a href=/builds/>Builds</a><a href=/visualtests/>Visual Tests</a><a class=font-mono target=_blank rel=noreferrer href=https://github.com/protomaps/basemaps>@');const Le="63c9e20".substr(0,8);function Re(e){return(()=>{var t=$e(),n=t.firstChild,s=n.nextSibling,r=s.nextSibling,i=r.nextSibling;return i.firstChild,ce(i,Le,null),H(o=>{var l=e.page===0?"font-bold":"underline",u=e.page===1?"font-bold":"underline",c=e.page===2?"font-bold":"underline";return l!==o.e&&J(n,o.e=l),u!==o.t&&J(s,o.t=u),c!==o.a&&J(r,o.a=c),o},{e:void 0,t:void 0,a:void 0}),t})()}const qe={4:[4,5],3:[3],2:[2],1:[1]};function Pe(e){const t={};for(const n of e.replace("#","").split("&")){const s=n.split("=");t[s[0]]=s[1]}return t}function We(e,t){const s={...Pe(e),...t};return`#${Object.entries(s).filter(([r,i])=>i!==void 0).map(([r,i])=>`${r}=${i}`).join("&")}`}async function Ge(e,t){return await(await fetch(`https://npm-style.protomaps.dev/layers.json?version=${e}&theme=${t||"light"}&lang=en`)).json()}const Ke=e=>e?!!(!e.startsWith("http")&&e.endsWith(".pmtiles")||e.startsWith("http")&&new URL(e).pathname.endsWith(".pmtiles")):!1;export{Be as F,Re as N,ke as S,qe as V,_ as a,we as b,Ve as c,De as d,We as e,H as f,B as g,Ke as h,ce as i,Ie as j,Ge as l,_e as o,Pe as p,Ue as r,Fe as s,Ne as t,He as u};
